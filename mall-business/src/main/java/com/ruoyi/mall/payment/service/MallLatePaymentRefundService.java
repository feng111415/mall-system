package com.ruoyi.mall.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

@Service
public class MallLatePaymentRefundService
{
    private final MallPaymentMapper paymentMapper;
    private final MallOrderMapper orderMapper;
    private final RefundPort refundPort;

    public MallLatePaymentRefundService(MallPaymentMapper paymentMapper, MallOrderMapper orderMapper,
            RefundPort refundPort)
    {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.refundPort = refundPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment refund(String paymentNo)
    {
        if (StringUtils.isBlank(paymentNo)) throw new ServiceException("支付单参数无效");
        MallPayment payment = paymentMapper.selectByPaymentNoForUpdate(paymentNo);
        if (payment == null) throw new ServiceException("支付单不存在");
        if ("REFUNDED".equals(payment.getStatus())) return payment;
        if (!"REFUNDING".equals(payment.getStatus())) throw new ServiceException("支付单不在异常退款中");
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), null);
        if (order == null || !"CLOSED".equals(order.getStatus())
                || !"REFUNDING".equals(order.getPaymentStatus()))
            throw new ServiceException("订单不在异常退款中");
        RefundPort.RefundResult result = refundPort.refund(order.getOrderNo(),
                payment.getPaymentNo(), payment.getAmount());
        if (result == null || !result.success() || StringUtils.isBlank(result.providerRefundNo()))
            throw new ServiceException(result == null ? "退款渠道未返回结果"
                    : StringUtils.isBlank(result.message()) ? "异常支付退款失败" : result.message());
        if (paymentMapper.markLatePaymentRefunded(payment.getPaymentId(), result.providerRefundNo()) != 1
                || orderMapper.markLatePaymentRefunded(order.getOrderId()) != 1)
            throw new ServiceException("异常支付退款结果保存失败");
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus("CLOSED"); log.setToStatus("CLOSED");
        log.setOperatorType("SYSTEM"); log.setOperatorId("late-payment-refund");
        log.setRemark("异常支付已原路退款"); log.setRequestId(payment.getPaymentNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        payment.setStatus("REFUNDED"); payment.setProviderRefundNo(result.providerRefundNo());
        order.setPaymentStatus("REFUNDED");
        return payment;
    }
}
