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

    public MallPaymentService(MallPaymentStateService stateService, PaymentPort paymentPort)
    {
        this.stateService = stateService;
        this.paymentPort = paymentPort;
    }

    public MallPayment create(Long memberId, Long orderId, MallCreatePaymentRequest request)
    {
        if (request == null || request.getIdempotencyKey() == null
                || request.getIdempotencyKey().trim().isEmpty() || request.getIdempotencyKey().length() > 80)
            throw new ServiceException("支付幂等键无效");
        MallPaymentStateService.PaymentBegin begin = stateService.beginPayment(memberId, orderId,
                request.getIdempotencyKey());
        if (!begin.shouldCallProvider()) return begin.payment();
        try
        {
            PaymentPort.PaymentCreateResult result = paymentPort.createPayment(begin.payment().getOrderNo(),
                    begin.payment().getAmount(), "商城订单 " + begin.payment().getOrderNo());
            return stateService.finishCreation(begin.payment().getPaymentId(), result);
        }
        catch (RuntimeException exception)
        {
            stateService.markCreationFailed(begin.payment().getPaymentId(), "支付渠道调用失败");
            throw exception;
        }
    }

    public MallPayment mockSuccess(Long memberId, String paymentNo)
    {
        return stateService.mockSuccess(memberId, paymentNo);
    }
}
