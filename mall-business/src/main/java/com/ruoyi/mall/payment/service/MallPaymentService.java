package com.ruoyi.mall.payment.service;

import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.PaymentPort;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.domain.dto.MallCreatePaymentRequest;

@Service
public class MallPaymentService
{
    private final MallPaymentStateService stateService;
    private final PaymentPort paymentPort;
    private final MallPaymentResultPreparationService resultPreparationService;
    private final MallLatePaymentRefundService latePaymentRefundService;

    public MallPaymentService(MallPaymentStateService stateService, PaymentPort paymentPort,
            MallPaymentResultPreparationService resultPreparationService,
            MallLatePaymentRefundService latePaymentRefundService)
    {
        this.stateService = stateService;
        this.paymentPort = paymentPort;
        this.resultPreparationService = resultPreparationService;
        this.latePaymentRefundService = latePaymentRefundService;
    }

    public MallPayment create(Long memberId, Long orderId, MallCreatePaymentRequest request)
    {
        if (request == null || request.getIdempotencyKey() == null
                || request.getIdempotencyKey().trim().isEmpty() || request.getIdempotencyKey().length() > 80)
            throw new ServiceException("支付幂等键无效");
        MallPaymentStateService.PaymentBegin begin = stateService.beginPayment(memberId, orderId,
                request.getIdempotencyKey());
        if (!begin.shouldCallProvider()) return begin.payment();
        PaymentPort.PaymentCreateResult result = paymentPort.createPayment(begin.payment().getPaymentNo(),
                begin.payment().getOrderNo(), begin.payment().getAmount(),
                "商城订单 " + begin.payment().getOrderNo());
        return stateService.finishCreation(begin.payment().getPaymentId(), result);
    }

    public MallPayment mockSuccess(Long memberId, String paymentNo)
    {
        MallPayment payment = resultPreparationService.recordMockSuccess(memberId, paymentNo);
        if (!"REFUNDING".equals(payment.getStatus())) return payment;
        try
        {
            return latePaymentRefundService.refund(payment.getPaymentNo());
        }
        catch (RuntimeException exception)
        {
            return payment;
        }
    }
}
