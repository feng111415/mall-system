package com.ruoyi.mall.aftersale.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.aftersale.domain.MallRefund;
import com.ruoyi.mall.aftersale.mapper.MallRefundMapper;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

@Service
public class MallRefundService
{
    private static final DateTimeFormatter REFUND_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final MallRefundMapper refundMapper;
    private final MallOrderMapper orderMapper;
    private final MallPaymentMapper paymentMapper;
    private final RefundPort refundPort;

    public MallRefundService(MallRefundMapper refundMapper, MallOrderMapper orderMapper,
            MallPaymentMapper paymentMapper, RefundPort refundPort)
    {
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.paymentMapper = paymentMapper;
        this.refundPort = refundPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallRefund apply(Long memberId, Long orderId, String reason)
    {
        requireMember(memberId);
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        MallOrder order = orderMapper.selectByIdForUpdate(orderId, memberId);
        if (order == null) throw new ServiceException("订单不存在");
        MallRefund existing = refundMapper.selectByOrderIdForUpdate(orderId);
        if (existing != null)
        {
            if (!"REJECTED".equals(existing.getStatus()) && !"FAILED".equals(existing.getStatus()))
                return existing;
            throw new ServiceException("该订单已有售后记录，请联系客服");
        }
        if (!("SHIPPED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus()))
                || !"PAID".equals(order.getPaymentStatus()))
            throw new ServiceException("当前订单不支持退款");
        if (StringUtils.isBlank(reason)) throw new ServiceException("退款原因不能为空");
        MallRefund refund = new MallRefund();
        refund.setRefundNo(generateRefundNo()); refund.setOrderId(order.getOrderId());
        refund.setOrderNo(order.getOrderNo()); refund.setMemberId(memberId);
        refund.setRefundAmount(order.getPayableAmount()); refund.setReason(normalizeReason(reason));
        refund.setStatus("APPLIED"); refund.setOriginalOrderStatus(order.getStatus());
        if (refundMapper.insert(refund) != 1)
        {
            existing = refundMapper.selectByOrderIdForUpdate(orderId);
            if (existing != null) return existing;
            throw new ServiceException("退款申请保存失败，请重试");
        }
        if (orderMapper.markRefunding(orderId, order.getStatus()) != 1)
            throw new ServiceException("订单状态已变化，请刷新后重试");
        writeLog(order, "AFTER_SALE", "MEMBER", String.valueOf(memberId), "提交退款申请");
        return refund;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallRefund approve(Long refundId, String operatorId)
    {
        MallRefund refund = lockRefund(refundId);
        if ("SUCCESS".equals(refund.getStatus())) return refund;
        if (!("APPLIED".equals(refund.getStatus()) || "REFUNDING".equals(refund.getStatus())))
            throw new ServiceException("当前退款单不允许审核");
        MallOrder order = orderMapper.selectByIdForUpdate(refund.getOrderId(), null);
        if (order == null || !"AFTER_SALE".equals(order.getStatus()) || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单当前不在退款处理中");
        if ("APPLIED".equals(refund.getStatus()) && refundMapper.markRefunding(refundId) != 1)
            throw new ServiceException("退款单状态已变化，请刷新后重试");
        MallPayment payment = paymentMapper.selectSuccessByOrderIdForUpdate(order.getOrderId());
        RefundPort.RefundResult result = refundPort.refund(order.getOrderNo(),
                payment == null ? null : payment.getPaymentNo(), refund.getRefundAmount());
        if (result == null || !result.success() || StringUtils.isBlank(result.providerRefundNo()))
        {
            String failure = result == null ? "退款渠道未返回结果"
                    : StringUtils.isBlank(result.providerRefundNo()) ? "退款渠道未返回退款流水号" : result.message();
            refundMapper.markFailed(refundId, failure);
            restoreOrderAfterFailure(order, refund.getOriginalOrderStatus(), operatorId, "退款渠道失败：" + failure);
            refund.setStatus("FAILED"); refund.setFailureReason(failure);
            return refund;
        }
        if (refundMapper.markSuccess(refundId, result.providerRefundNo()) != 1)
            throw new ServiceException("退款结果保存失败");
        if (orderMapper.markRefundSuccess(order.getOrderId()) != 1)
            throw new ServiceException("订单退款状态保存失败");
        writeLog(order, "AFTER_SALE", "ADMIN", operatorId, "退款成功");
        refund.setStatus("SUCCESS"); refund.setProviderRefundNo(result.providerRefundNo());
        refund.setRefundTime(LocalDateTime.now());
        return refund;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallRefund reject(Long refundId, String operatorId, String reason)
    {
        MallRefund refund = lockRefund(refundId);
        if ("REJECTED".equals(refund.getStatus())) return refund;
        if (!"APPLIED".equals(refund.getStatus()) && !"REFUNDING".equals(refund.getStatus()))
            throw new ServiceException("当前退款单不允许拒绝");
        MallOrder order = orderMapper.selectByIdForUpdate(refund.getOrderId(), null);
        if (order == null || !"AFTER_SALE".equals(order.getStatus()) || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单当前不在退款处理中");
        String rejectReason = StringUtils.isBlank(reason) ? "售后申请未通过" : normalizeReason(reason);
        if (refundMapper.markRejected(refundId, rejectReason) != 1)
            throw new ServiceException("退款单状态已变化，请刷新后重试");
        if (orderMapper.restoreAfterRefundReject(order.getOrderId(), refund.getOriginalOrderStatus()) != 1)
            throw new ServiceException("订单状态恢复失败");
        writeLog(order, refund.getOriginalOrderStatus(), "ADMIN", operatorId, rejectReason);
        refund.setStatus("REJECTED"); refund.setFailureReason(rejectReason);
        return refund;
    }

    public List<MallRefund> list(Long memberId)
    {
        requireMember(memberId);
        return refundMapper.selectMemberList(memberId);
    }

    public MallRefund detail(Long memberId, Long refundId)
    {
        requireMember(memberId);
        if (refundId == null || refundId <= 0) throw new ServiceException("退款单参数无效");
        MallRefund refund = refundMapper.selectMemberById(refundId, memberId);
        if (refund == null) throw new ServiceException("退款单不存在");
        return refund;
    }

    private MallRefund lockRefund(Long refundId)
    {
        if (refundId == null || refundId <= 0) throw new ServiceException("退款单参数无效");
        MallRefund refund = refundMapper.selectByIdForUpdate(refundId);
        if (refund == null) throw new ServiceException("退款单不存在");
        return refund;
    }

    private void restoreOrderAfterFailure(MallOrder order, String originalStatus, String operatorId, String reason)
    {
        if (orderMapper.restoreAfterRefundReject(order.getOrderId(), originalStatus) != 1)
            throw new ServiceException("退款失败后订单状态恢复失败");
        writeLog(order, originalStatus, "SYSTEM", operatorId, reason);
    }

    private void writeLog(MallOrder order, String toStatus, String operatorType, String operatorId, String remark)
    {
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus(order.getStatus()); log.setToStatus(toStatus);
        log.setOperatorType(operatorType); log.setOperatorId(operatorId); log.setRemark(remark);
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
    }

    private String generateRefundNo()
    {
        return "REF" + REFUND_NO_TIME.format(LocalDateTime.now())
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    private String normalizeReason(String reason)
    {
        String value = reason.trim();
        return value.length() > 255 ? value.substring(0, 255) : value;
    }

    private void requireMember(Long memberId)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
    }
}
