package com.ruoyi.mall.order.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;

@Service
public class MallOrderLifecycleService
{
    private static final int TIMEOUT_BATCH_SIZE = 100;
    private static final int PAYMENT_TIMEOUT_MINUTES = 30;
    private final MallOrderMapper mapper;
    private final InventoryPort inventoryPort;

    public MallOrderLifecycleService(MallOrderMapper mapper, InventoryPort inventoryPort)
    {
        this.mapper = mapper;
        this.inventoryPort = inventoryPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallOrder cancelByMember(Long memberId, Long orderId, String reason)
    {
        if (memberId == null || memberId <= 0 || orderId == null || orderId <= 0)
            throw new ServiceException("订单参数无效");
        MallOrder order = mapper.selectByIdForUpdate(orderId, memberId);
        if (order == null) throw new ServiceException("订单不存在");
        ensureUnpaidPending(order);
        close(order, MallOrderStatus.CANCELLED.name(), normalizeReason(reason, "会员主动取消"),
                "MEMBER", String.valueOf(memberId));
        return order;
    }

    /** Invoked by a Quartz job; each run safely closes at most one batch of expired orders. */
    @Transactional(rollbackFor = Exception.class)
    public int closeExpiredOrders()
    {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<MallOrder> candidates = mapper.selectTimeoutCandidates(cutoffTime, TIMEOUT_BATCH_SIZE);
        int closed = 0;
        for (MallOrder candidate : candidates)
        {
            MallOrder order = mapper.selectByIdForUpdate(candidate.getOrderId(), null);
            if (order == null || !isUnpaidPending(order) || order.getCreateTime() == null
                    || order.getCreateTime().isAfter(cutoffTime)) continue;
            close(order, MallOrderStatus.CLOSED.name(), "支付超时自动关闭", "SYSTEM", "order-timeout");
            closed++;
        }
        return closed;
    }

    private void close(MallOrder order, String targetStatus, String reason,
            String operatorType, String operatorId)
    {
        if (inventoryPort == null) throw new IllegalStateException("InventoryPort 未配置");
        if (mapper.updateStatus(order.getOrderId(), order.getStatus(), targetStatus, reason) != 1)
            throw new ServiceException("订单状态已变化，请刷新后重试");
        inventoryPort.release(order.getOrderNo());

        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus(order.getStatus()); log.setToStatus(targetStatus);
        log.setOperatorType(operatorType); log.setOperatorId(operatorId); log.setRemark(reason);
        if (mapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        order.setStatus(targetStatus); order.setCancelReason(reason); order.setCloseTime(LocalDateTime.now());
        order.setVersion(order.getVersion() == null ? 1 : order.getVersion() + 1);
    }

    private void ensureUnpaidPending(MallOrder order)
    {
        if (!isUnpaidPending(order)) throw new ServiceException("当前订单状态不允许取消");
    }

    private boolean isUnpaidPending(MallOrder order)
    {
        return order != null && MallOrderStatus.PENDING_PAYMENT.name().equals(order.getStatus())
                && "UNPAID".equals(order.getPaymentStatus());
    }

    private String normalizeReason(String reason, String fallback)
    {
        if (StringUtils.isBlank(reason)) return fallback;
        return reason.length() > 255 ? reason.substring(0, 255) : reason;
    }
}
