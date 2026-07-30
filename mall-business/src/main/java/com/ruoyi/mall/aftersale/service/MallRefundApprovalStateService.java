package com.ruoyi.mall.aftersale.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.aftersale.domain.MallRefund;
import com.ruoyi.mall.aftersale.mapper.MallRefundMapper;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

/** Persists refund state around an external refund call without holding database locks during I/O. */
@Service
public class MallRefundApprovalStateService
{
    private final MallRefundMapper refundMapper;
    private final MallOrderMapper orderMapper;
    private final MallPaymentMapper paymentMapper;

    public MallRefundApprovalStateService(MallRefundMapper refundMapper, MallOrderMapper orderMapper,
            MallPaymentMapper paymentMapper)
    {
        this.refundMapper = refundMapper;
        this.orderMapper = orderMapper;
        this.paymentMapper = paymentMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public RefundApproval prepare(Long refundId, String operatorId)
    {
        MallRefund refund = requireRefund(refundId);
        if ("SUCCESS".equals(refund.getStatus())) return RefundApproval.completed(refund);
        if (!("APPLIED".equals(refund.getStatus()) || "REFUNDING".equals(refund.getStatus())))
            throw new ServiceException("当前退款单不允许审核");
        MallOrder order = requireRefundingOrder(refund);
        if ("APPLIED".equals(refund.getStatus()) && refundMapper.markRefunding(refundId) != 1)
            throw new ServiceException("退款单状态已变化，请刷新后重试");
        MallPayment payment = paymentMapper.selectSuccessByOrderIdForUpdate(order.getOrderId());
        if (payment == null || payment.getPaymentNo() == null)
            throw new ServiceException("原支付单不存在，不能发起退款");
        return RefundApproval.pending(refund, order.getOrderNo(), payment.getPaymentNo(),
                refund.getRefundAmount(), operatorId);
    }

    @Transactional(rollbackFor = Exception.class)
    public MallRefund completeSuccess(RefundApproval approval, String providerRefundNo)
    {
        MallRefund refund = requireRefund(approval.refundId());
        if ("SUCCESS".equals(refund.getStatus())) return refund;
        if (!"REFUNDING".equals(refund.getStatus()))
            throw new ServiceException("退款单当前状态不允许确认成功");
        MallOrder order = requireRefundingOrder(refund);
        if (refundMapper.markSuccess(refund.getRefundId(), providerRefundNo) != 1
                || orderMapper.markRefundSuccess(order.getOrderId()) != 1)
            throw new ServiceException("退款结果保存失败");
        writeLog(order, "AFTER_SALE", "ADMIN", approval.operatorId(), "退款成功");
        refund.setStatus("SUCCESS");
        refund.setProviderRefundNo(providerRefundNo);
        refund.setRefundTime(LocalDateTime.now());
        return refund;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallRefund completeFailure(RefundApproval approval, String reason)
    {
        MallRefund refund = requireRefund(approval.refundId());
        if ("SUCCESS".equals(refund.getStatus())) return refund;
        if (!"REFUNDING".equals(refund.getStatus()))
            throw new ServiceException("退款单当前状态不允许确认失败");
        MallOrder order = requireRefundingOrder(refund);
        if (refundMapper.markFailed(refund.getRefundId(), reason) != 1)
            throw new ServiceException("退款失败状态保存失败");
        if (orderMapper.restoreAfterRefundReject(order.getOrderId(), refund.getOriginalOrderStatus()) != 1)
            throw new ServiceException("退款失败后订单状态恢复失败");
        writeLog(order, refund.getOriginalOrderStatus(), "SYSTEM", approval.operatorId(), "退款渠道失败：" + reason);
        refund.setStatus("FAILED");
        refund.setFailureReason(reason);
        return refund;
    }

    private MallRefund requireRefund(Long refundId)
    {
        if (refundId == null || refundId <= 0) throw new ServiceException("退款单参数无效");
        MallRefund refund = refundMapper.selectByIdForUpdate(refundId);
        if (refund == null) throw new ServiceException("退款单不存在");
        return refund;
    }

    private MallOrder requireRefundingOrder(MallRefund refund)
    {
        MallOrder order = orderMapper.selectByIdForUpdate(refund.getOrderId(), null);
        if (order == null || !"AFTER_SALE".equals(order.getStatus()) || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单当前不在退款处理中");
        return order;
    }

    private void writeLog(MallOrder order, String toStatus, String operatorType, String operatorId, String remark)
    {
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(order.getStatus());
        log.setToStatus(toStatus);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setRemark(remark);
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
    }

    public record RefundApproval(Long refundId, String refundNo, String orderNo, String paymentNo,
            BigDecimal amount, String operatorId, MallRefund completedRefund)
    {
        static RefundApproval pending(MallRefund refund, String orderNo, String paymentNo,
                BigDecimal amount, String operatorId)
        {
            return new RefundApproval(refund.getRefundId(), refund.getRefundNo(), orderNo, paymentNo,
                    amount, operatorId, null);
        }

        static RefundApproval completed(MallRefund refund)
        {
            return new RefundApproval(refund.getRefundId(), refund.getRefundNo(), null, null,
                    null, null, refund);
        }

        public boolean shouldCallProvider() { return completedRefund == null; }
    }
}
