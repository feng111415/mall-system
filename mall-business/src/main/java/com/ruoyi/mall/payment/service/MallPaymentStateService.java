package com.ruoyi.mall.payment.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.application.port.PaymentExpirationPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.domain.MallOrderPaymentWindow;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.domain.MallPaymentStatus;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.application.port.PaymentPort.PaymentCreateResult;

@Service
public class MallPaymentStateService implements PaymentExpirationPort
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
        if (!MallOrderPaymentWindow.canCreatePayment(order, LocalDateTime.now()))
            throw new ServiceException("订单支付创建期限已结束");
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
        if (orderMapper.markPaymentPaying(order.getOrderId()) != 1)
            throw new ServiceException("订单状态已变化，请刷新后重试");
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
            orderMapper.resetPaymentUnpaid(payment.getOrderId());
            payment.setStatus(MallPaymentStatus.FAILED.name());
            payment.setFailureReason(result == null ? "支付渠道未返回结果" : result.message());
            return payment;
        }
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), payment.getMemberId());
        if (!isPaymentCreationPending(order, payment.getAmount()))
        {
            paymentMapper.updateCreationResult(paymentId, MallPaymentStatus.FAILED.name(), result.paymentNo(),
                    result.paymentUrl(), "订单已关闭或金额已变化");
            payment.setStatus(MallPaymentStatus.FAILED.name()); payment.setFailureReason("订单已关闭或金额已变化");
            return payment;
        }
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
            orderMapper.resetPaymentUnpaid(payment.getOrderId());
            payment.setStatus(MallPaymentStatus.FAILED.name()); payment.setFailureReason(reason);
        }
        return payment;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closePendingPayment(Long orderId)
    {
        return orderId != null && paymentMapper.closePendingByOrder(orderId) > 0;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment mockSuccess(Long memberId, String paymentNo)
    {
        MallPayment payment = paymentMapper.selectByPaymentNoForUpdate(paymentNo);
        if (payment == null || !memberId.equals(payment.getMemberId())) throw new ServiceException("支付单不存在");
        if (MallPaymentStatus.SUCCESS.name().equals(payment.getStatus())) return payment;
        if (MallPaymentStatus.REFUNDING.name().equals(payment.getStatus())
                || MallPaymentStatus.REFUNDED.name().equals(payment.getStatus())) return payment;
        if (MallPaymentStatus.CLOSED.name().equals(payment.getStatus()))
            return recordLateSuccess(payment, memberId);
        if (!MallPaymentStatus.PAYING.name().equals(payment.getStatus()))
            throw new ServiceException("当前支付单不可确认支付");
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), memberId);
        if (!isPayingOrder(order, payment.getAmount()))
            throw new ServiceException("订单状态不允许支付");
        if (MallOrderPaymentWindow.isResultExpired(order, LocalDateTime.now()))
        {
            closeForLatePayment(order, payment);
            return recordLateSuccess(payment, memberId);
        }
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

    private void closeForLatePayment(MallOrder order, MallPayment payment)
    {
        String reason = "支付结果等待超时自动关闭";
        if (orderMapper.closeExpired(order.getOrderId(), order.getPaymentStatus(), reason) != 1)
            throw new ServiceException("订单超时状态保存失败，请重试");
        inventoryPort.release(order.getOrderNo());
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus("PENDING_PAYMENT"); log.setToStatus("CLOSED");
        log.setOperatorType("SYSTEM"); log.setOperatorId("payment-deadline");
        log.setRemark(reason); log.setRequestId(payment.getPaymentNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        order.setStatus("CLOSED"); order.setPaymentStatus("FAILED");
        order.setCloseTime(LocalDateTime.now());
    }

    private MallPayment recordLateSuccess(MallPayment payment, Long memberId)
    {
        MallOrder order = orderMapper.selectByIdForUpdate(payment.getOrderId(), memberId);
        if (order == null || !"CLOSED".equals(order.getStatus()) || !"FAILED".equals(order.getPaymentStatus()))
            throw new ServiceException("订单状态不允许处理迟到支付");
        if (paymentMapper.markLatePaymentRefunding(payment.getPaymentId()) != 1
                || orderMapper.markLatePaymentRefunding(order.getOrderId()) != 1)
            throw new ServiceException("迟到支付状态保存失败，请重试");
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus("CLOSED"); log.setToStatus("CLOSED");
        log.setOperatorType("PAYMENT"); log.setOperatorId(payment.getPaymentNo());
        log.setRemark("订单关闭后收到支付成功结果，自动原路退款");
        log.setRequestId(payment.getPaymentNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        payment.setStatus(MallPaymentStatus.REFUNDING.name()); payment.setPaidTime(LocalDateTime.now());
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

    private boolean isPaymentCreationPending(MallOrder order, BigDecimal amount)
    {
        return isPayingOrder(order, amount);
    }

    private String generatePaymentNo()
    {
        return "PAY" + PAYMENT_NO_TIME.format(LocalDateTime.now())
                + String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    public record PaymentBegin(MallPayment payment, boolean shouldCallProvider) { }
}
