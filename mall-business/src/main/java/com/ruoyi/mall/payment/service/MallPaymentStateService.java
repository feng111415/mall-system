package com.ruoyi.mall.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.domain.MallPaymentStatus;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.application.port.PaymentPort.PaymentCreateResult;

@Service
public class MallPaymentStateService
{
    private static final DateTimeFormatter PAYMENT_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final MallPaymentMapper paymentMapper;
    private final MallOrderMapper orderMapper;
    private final InventoryPort inventoryPort;

    public MallPaymentStateService(MallPaymentMapper paymentMapper, MallOrderMapper orderMapper,
            InventoryPort inventoryPort)
    {
        this.paymentMapper = paymentMapper;
        this.orderMapper = orderMapper;
        this.inventoryPort = inventoryPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public PaymentBegin beginPayment(Long memberId, Long orderId, String idempotencyKey)
    {
        MallPayment existing = paymentMapper.selectByOrderIdempotency(orderId, memberId, idempotencyKey);
        if (existing != null) return new PaymentBegin(existing, false);
        MallOrder order = requirePayableOrder(orderId, memberId);
        MallPayment payment = new MallPayment();
        payment.setPaymentNo(generatePaymentNo()); payment.setOrderId(order.getOrderId());
        payment.setOrderNo(order.getOrderNo()); payment.setMemberId(memberId);
        payment.setPaymentMethod("MOCK"); payment.setAmount(order.getPayableAmount());
        payment.setStatus(MallPaymentStatus.CREATING.name()); payment.setIdempotencyKey(idempotencyKey);
        if (paymentMapper.insertPayment(payment) == 0)
        {
            existing = paymentMapper.selectByOrderIdempotency(orderId, memberId, idempotencyKey);
            if (existing != null) return new PaymentBegin(existing, false);
            throw new ServiceException("支付单创建失败，请重试");
        }
        return new PaymentBegin(payment, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment finishCreation(Long paymentId, PaymentCreateResult result)
    {
        MallPayment payment = findPaymentForUpdate(paymentId);
        if (!MallPaymentStatus.CREATING.name().equals(payment.getStatus())) return payment;
        if (result == null || !result.success())
        {
            paymentMapper.updateCreationResult(paymentId, MallPaymentStatus.FAILED.name(), null, null,
                    result == null ? "支付渠道未返回结果" : result.message());
            payment.setStatus(MallPaymentStatus.FAILED.name());
            payment.setFailureReason(result == null ? "支付渠道未返回结果" : result.message());
            return payment;
        }
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), payment.getMemberId());
        if (!isPayableOrder(order, payment.getAmount()))
        {
            paymentMapper.updateCreationResult(paymentId, MallPaymentStatus.FAILED.name(), result.paymentNo(),
                    result.paymentUrl(), "订单已关闭或金额已变化");
            payment.setStatus(MallPaymentStatus.FAILED.name()); payment.setFailureReason("订单已关闭或金额已变化");
            return payment;
        }
        if (orderMapper.markPaymentPaying(order.getOrderId()) != 1)
            throw new ServiceException("订单状态已变化，请刷新后重试");
        paymentMapper.updateCreationResult(paymentId, MallPaymentStatus.PAYING.name(), result.paymentNo(),
                result.paymentUrl(), null);
        payment.setStatus(MallPaymentStatus.PAYING.name()); payment.setProviderPaymentNo(result.paymentNo());
        payment.setPaymentUrl(result.paymentUrl());
        return payment;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment markCreationFailed(Long paymentId, String reason)
    {
        MallPayment payment = findPaymentForUpdate(paymentId);
        if (MallPaymentStatus.CREATING.name().equals(payment.getStatus()))
        {
            paymentMapper.updateCreationResult(paymentId, MallPaymentStatus.FAILED.name(), null, null, reason);
            payment.setStatus(MallPaymentStatus.FAILED.name()); payment.setFailureReason(reason);
        }
        return payment;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment mockSuccess(Long memberId, String paymentNo)
    {
        MallPayment payment = paymentMapper.selectByPaymentNoForUpdate(paymentNo);
        if (payment == null || !memberId.equals(payment.getMemberId())) throw new ServiceException("支付单不存在");
        if (MallPaymentStatus.SUCCESS.name().equals(payment.getStatus())) return payment;
        if (!MallPaymentStatus.PAYING.name().equals(payment.getStatus()))
            throw new ServiceException("当前支付单不可确认支付");
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), memberId);
        if (!isPayingOrder(order, payment.getAmount()))
            throw new ServiceException("订单状态不允许支付");
        if (orderMapper.markPaymentSuccess(order.getOrderId()) != 1)
            throw new ServiceException("订单支付状态已变化，请刷新后重试");
        if (paymentMapper.updateSuccess(payment.getPaymentId()) != 1)
            throw new ServiceException("支付结果保存失败，请重试");
        inventoryPort.confirm(order.getOrderNo(), true);
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus("PENDING_PAYMENT"); log.setToStatus("PENDING_SHIPMENT");
        log.setOperatorType("PAYMENT"); log.setOperatorId(payment.getPaymentNo());
        log.setRemark("模拟支付成功"); log.setRequestId(payment.getPaymentNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        payment.setStatus(MallPaymentStatus.SUCCESS.name()); payment.setPaidTime(LocalDateTime.now());
        return payment;
    }

    private MallPayment findPaymentForUpdate(Long paymentId)
    {
        MallPayment payment = paymentMapper.selectByIdForUpdate(paymentId);
        if (payment == null) throw new ServiceException("支付单不存在");
        return payment;
    }

    private MallOrder requirePayableOrder(Long orderId, Long memberId)
    {
        if (orderId == null || orderId <= 0 || memberId == null || memberId <= 0)
            throw new ServiceException("订单参数无效");
        MallOrder order = orderMapper.selectByIdForUpdate(orderId, memberId);
        if (!isPayableOrder(order, order == null ? null : order.getPayableAmount()))
            throw new ServiceException("当前订单不可支付");
        return order;
    }

    private boolean isPayableOrder(MallOrder order, BigDecimal amount)
    {
        return order != null && "PENDING_PAYMENT".equals(order.getStatus())
                && "UNPAID".equals(order.getPaymentStatus()) && order.getPayableAmount() != null
                && order.getPayableAmount().compareTo(amount == null ? BigDecimal.valueOf(-1) : amount) == 0;
    }

    private boolean isPayingOrder(MallOrder order, BigDecimal amount)
    {
        return order != null && "PENDING_PAYMENT".equals(order.getStatus())
                && "PAYING".equals(order.getPaymentStatus()) && order.getPayableAmount() != null
                && order.getPayableAmount().compareTo(amount == null ? BigDecimal.valueOf(-1) : amount) == 0;
    }

    private String generatePaymentNo()
    {
        return "PAY" + PAYMENT_NO_TIME.format(LocalDateTime.now())
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    public record PaymentBegin(MallPayment payment, boolean shouldCallProvider) { }
}
