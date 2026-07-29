package com.ruoyi.mall.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.application.port.PaymentPort;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.service.MallLatePaymentRefundService;
import com.ruoyi.mall.payment.service.MallPaymentResultPreparationService;
import com.ruoyi.mall.payment.service.MallPaymentService;
import com.ruoyi.mall.payment.service.MallPaymentStateService;

class MallPaymentServiceTest
{
    @Mock private MallPaymentStateService stateService;
    @Mock private PaymentPort paymentPort;
    @Mock private MallPaymentResultPreparationService resultPreparationService;
    @Mock private MallLatePaymentRefundService latePaymentRefundService;
    private MallPaymentService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallPaymentService(stateService, paymentPort,
                resultPreparationService, latePaymentRefundService);
    }

    @Test
    void lateSuccessPersistsCompensationBeforeImmediateRefund()
    {
        MallPayment refunding = payment("REFUNDING");
        MallPayment refunded = payment("REFUNDED");
        when(resultPreparationService.recordMockSuccess(7L, "PAY-1")).thenReturn(refunding);
        when(latePaymentRefundService.refund("PAY-1")).thenReturn(refunded);

        MallPayment result = service.mockSuccess(7L, "PAY-1");

        assertEquals("REFUNDED", result.getStatus());
        org.mockito.Mockito.verify(latePaymentRefundService).refund("PAY-1");
    }

    private MallPayment payment(String status)
    {
        MallPayment payment = new MallPayment();
        payment.setPaymentNo("PAY-1"); payment.setOrderNo("ORDER-1"); payment.setStatus(status);
        return payment;
    }
}
