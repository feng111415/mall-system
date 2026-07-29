package com.ruoyi.mall.governance.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.payment.service.MallLatePaymentRefundService;
import com.ruoyi.mall.aftersale.domain.MallRefund;
import com.ruoyi.mall.aftersale.mapper.MallRefundMapper;
import com.ruoyi.mall.governance.domain.MallCompensationTask;
import com.ruoyi.mall.governance.mapper.MallCompensationMapper;

@Service
public class MallCompensationService
{
    private static final int BATCH_SIZE = 50;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private final MallCompensationMapper mapper;
    private final InventoryPort inventoryPort;
    private final MallPaymentMapper paymentMapper;
    private final MallRefundMapper refundMapper;
    private final MallOrderMapper orderMapper;
    private final MallLatePaymentRefundService latePaymentRefundService;

    /** Constructor retained for isolated inventory compensation tests. */
    public MallCompensationService(MallCompensationMapper mapper, InventoryPort inventoryPort)
    {
        this(mapper, inventoryPort, null, null, null, null);
    }

    @Autowired
    public MallCompensationService(MallCompensationMapper mapper, InventoryPort inventoryPort,
            MallPaymentMapper paymentMapper, MallRefundMapper refundMapper, MallOrderMapper orderMapper,
            MallLatePaymentRefundService latePaymentRefundService)
    {
        this.mapper = mapper;
        this.inventoryPort = inventoryPort;
        this.paymentMapper = paymentMapper;
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.latePaymentRefundService = latePaymentRefundService;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCompensationTask create(String taskType, String businessKey, String orderNo, String payload)
    {
        if (StringUtils.isBlank(taskType) || StringUtils.isBlank(businessKey))
            throw new ServiceException("补偿任务参数无效");
        MallCompensationTask existing = mapper.selectByBusinessKey(taskType, businessKey);
        if (existing != null) return existing;
        MallCompensationTask task = new MallCompensationTask();
        task.setTaskNo("COMP-" + UUID.randomUUID().toString().replace("-", ""));
        task.setTaskType(taskType.trim().toUpperCase()); task.setBusinessKey(businessKey.trim());
        task.setOrderNo(orderNo); task.setPayload(payload); task.setMaxRetries(DEFAULT_MAX_RETRIES);
        if (mapper.insertIgnore(task) != 1)
        {
            existing = mapper.selectByBusinessKey(task.getTaskType(), task.getBusinessKey());
            if (existing != null) return existing;
            throw new ServiceException("补偿任务保存失败");
        }
        task.setStatus("PENDING"); task.setRetryCount(0);
        return task;
    }

    public int runDueTasks()
    {
        mapper.recoverStaleProcessing(LocalDateTime.now().minusMinutes(10));
        int processed = 0;
        for (MallCompensationTask candidate : mapper.selectDue(BATCH_SIZE))
        {
            if (mapper.markProcessing(candidate.getTaskId()) != 1) continue;
            processed++;
            try
            {
                execute(candidate);
                if (mapper.markSuccess(candidate.getTaskId()) != 1)
                    throw new ServiceException("补偿任务成功状态保存失败");
            }
            catch (RuntimeException exception)
            {
                int retry = (candidate.getRetryCount() == null ? 0 : candidate.getRetryCount()) + 1;
                boolean manual = retry >= (candidate.getMaxRetries() == null ? DEFAULT_MAX_RETRIES : candidate.getMaxRetries());
                LocalDateTime next = LocalDateTime.now().plusMinutes(Math.min(60, 1L << Math.min(retry, 6)));
                mapper.markFailure(candidate.getTaskId(), manual ? "MANUAL" : "FAILED", next,
                        normalizeError(exception.getMessage()));
            }
        }
        return processed;
    }

    public List<MallCompensationTask> list(String status, String taskType, Integer limit, Integer offset)
    {
        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 100);
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        return mapper.selectList(status, taskType, safeLimit, safeOffset);
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCompensationTask retry(Long taskId)
    {
        if (taskId == null || taskId <= 0) throw new ServiceException("补偿任务参数无效");
        MallCompensationTask task = mapper.selectById(taskId);
        if (task == null) throw new ServiceException("补偿任务不存在");
        if ("SUCCESS".equals(task.getStatus())) return task;
        if (mapper.resetForRetry(taskId) != 1) throw new ServiceException("当前任务不允许重试");
        task.setStatus("PENDING"); task.setRetryCount(0); task.setLastError(null);
        return task;
    }

    private void execute(MallCompensationTask task)
    {
        switch (task.getTaskType())
        {
            case "INVENTORY_RELEASE" -> { requireInventoryDependency(); inventoryPort.release(requireBusinessKey(task)); }
            case "INVENTORY_CONFIRM" -> { requireInventoryDependency(); inventoryPort.confirm(requireBusinessKey(task), true); }
            case "PAYMENT_CONFIRM" -> confirmPayment(task);
            case "REFUND_CONFIRM" -> confirmRefund(task);
            case "LATE_PAYMENT_REFUND" -> refundLatePayment(task);
            default -> throw new ServiceException("暂未注册补偿处理器：" + task.getTaskType());
        }
    }

    private void refundLatePayment(MallCompensationTask task)
    {
        if (latePaymentRefundService == null) throw new IllegalStateException("异常支付退款依赖未配置");
        latePaymentRefundService.refund(requireBusinessKey(task));
    }

    private void requireInventoryDependency()
    {
        if (inventoryPort == null) throw new IllegalStateException("InventoryPort 未配置");
    }

    private void confirmPayment(MallCompensationTask task)
    {
        requireGovernanceDependencies(paymentMapper, orderMapper);
        MallPayment payment = paymentMapper.selectByPaymentNoForUpdate(requireBusinessKey(task));
        if (payment == null) throw new ServiceException("支付单不存在");
        if ("SUCCESS".equals(payment.getStatus())) return;
        if (!"PAYING".equals(payment.getStatus())) throw new ServiceException("支付单状态不允许补偿");
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), null);
        if (order == null) throw new ServiceException("订单不存在");
        if ("PENDING_SHIPMENT".equals(order.getStatus()) && "PAID".equals(order.getPaymentStatus())) return;
        if (!"PENDING_PAYMENT".equals(order.getStatus()) || !"PAYING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单状态不允许支付补偿");
        if (orderMapper.markPaymentSuccess(order.getOrderId()) != 1)
            throw new ServiceException("订单支付状态补偿失败");
        if (paymentMapper.updateSuccess(payment.getPaymentId()) != 1)
            throw new ServiceException("支付单状态补偿失败");
        requireInventoryDependency();
        inventoryPort.confirm(order.getOrderNo(), true);
    }

    private void confirmRefund(MallCompensationTask task)
    {
        requireGovernanceDependencies(refundMapper, orderMapper);
        MallRefund refund = refundMapper.selectByRefundNoForUpdate(requireBusinessKey(task));
        if (refund == null) throw new ServiceException("退款单不存在");
        if ("SUCCESS".equals(refund.getStatus())) return;
        if (!"REFUNDING".equals(refund.getStatus())) throw new ServiceException("退款单状态不允许补偿");
        MallOrder order = orderMapper.selectByIdForUpdate(refund.getOrderId(), null);
        if (order == null || !"AFTER_SALE".equals(order.getStatus()) || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单状态不允许退款补偿");
        if (StringUtils.isBlank(refund.getProviderRefundNo()))
            throw new ServiceException("缺少外部退款流水号，不能跳过渠道确认");
        if (refundMapper.markSuccess(refund.getRefundId(), refund.getProviderRefundNo()) != 1)
            throw new ServiceException("退款单状态补偿失败");
        if (orderMapper.markRefundSuccess(order.getOrderId()) != 1)
            throw new ServiceException("订单退款状态补偿失败");
    }

    private String requireBusinessKey(MallCompensationTask task)
    {
        if (StringUtils.isBlank(task.getBusinessKey())) throw new ServiceException("补偿业务键为空");
        return task.getBusinessKey();
    }

    private void requireGovernanceDependencies(Object payment, Object order)
    {
        if (payment == null || order == null) throw new IllegalStateException("支付退款补偿依赖未配置");
    }

    private String normalizeError(String error)
    {
        String value = StringUtils.isBlank(error) ? "补偿执行失败" : error;
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
