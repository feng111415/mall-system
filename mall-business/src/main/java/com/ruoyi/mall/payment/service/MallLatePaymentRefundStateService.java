package com.ruoyi.mall.payment.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

/** Persists late-payment refund transitions before and after external channel I/O. */
@Service
public class MallLatePaymentRefundStateService
{
    private final MallPaymentMapper paymentMapper;
    private final MallOrderMapper orderMapper;

    public MallLatePaymentRefundStateService(MallPaymentMapper paymentMapper, MallOrderMapper orderMapper)
    {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public LateRefundPreparation prepare(String paymentNo)
    {
        MallPayment payment = paymentMapper.selectByPaymentNoForUpdate(paymentNo);
        if (payment == null) throw new ServiceException("支付单不存在");
        if ("REFUNDED".equals(payment.getStatus())) return LateRefundPreparation.completed(payment);
        if (!"REFUNDING".equals(payment.getStatus())) throw new ServiceException("支付单不在异常退款中");
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), null);
        if (order == null || !"CLOSED".equals(order.getStatus()) || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单不在异常退款中");
        return LateRefundPreparation.pending(payment, order.getOrderNo(), payment.getAmount());
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment completeSuccess(LateRefundPreparation preparation, String providerRefundNo)
    {
        MallPayment payment = paymentMapper.selectByPaymentNoForUpdate(preparation.paymentNo());
        if (payment == null) throw new ServiceException("支付单不存在");
        if ("REFUNDED".equals(payment.getStatus())) return payment;
        if (!"REFUNDING".equals(payment.getStatus())) throw new ServiceException("支付单状态不允许确认退款");
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), null);
        if (order == null || !"CLOSED".equals(order.getStatus()) || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单不在异常退款中");
        if (paymentMapper.markLatePaymentRefunded(payment.getPaymentId(), providerRefundNo) != 1
                || orderMapper.markLatePaymentRefunded(order.getOrderId()) != 1)
            throw new ServiceException("异常支付退款结果保存失败");
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus("CLOSED"); log.setToStatus("CLOSED");
        log.setOperatorType("SYSTEM"); log.setOperatorId("late-payment-refund");
        log.setRemark("异常支付已原路退款"); log.setRequestId(payment.getPaymentNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        payment.setStatus("REFUNDED"); payment.setProviderRefundNo(providerRefundNo);
        return payment;
    }

    public record LateRefundPreparation(String paymentNo, String refundNo, String orderNo,
            BigDecimal amount, MallPayment completedPayment)
    {
        static LateRefundPreparation pending(MallPayment payment, String orderNo, BigDecimal amount)
        {
            return new LateRefundPreparation(payment.getPaymentNo(), "LATE-" + payment.getPaymentNo(),
                    orderNo, amount, null);
        }

        static LateRefundPreparation completed(MallPayment payment)
        {
            return new LateRefundPreparation(payment.getPaymentNo(), null, null, null, payment);
        }

        public boolean shouldCallProvider() { return completedPayment == null; }
    }
}
