package com.ruoyi.mall.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mall.governance.service.MallCompensationService;
import com.ruoyi.mall.payment.domain.MallPayment;

/** Atomically records a payment result and any compensation work it requires. */
@Service
public class MallPaymentResultPreparationService
{
    private final MallPaymentStateService stateService;
    private final MallCompensationService compensationService;

    public MallPaymentResultPreparationService(MallPaymentStateService stateService,
            MallCompensationService compensationService)
    {
        this.stateService = stateService;
        this.compensationService = compensationService;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallPayment recordMockSuccess(Long memberId, String paymentNo)
    {
        MallPayment payment = stateService.mockSuccess(memberId, paymentNo);
        if ("REFUNDING".equals(payment.getStatus()))
        {
            compensationService.create("LATE_PAYMENT_REFUND", payment.getPaymentNo(), payment.getOrderNo(),
                    "订单关闭后收到支付成功结果");
        }
        return payment;
    }
}
