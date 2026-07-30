package com.ruoyi.mall.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.application.port.PaymentPort;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.domain.dto.MallCreatePaymentRequest;
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

    @Test
    void providerReceivesPaymentNumberAsIdempotencyKey()
    {
        MallPayment payment = payment("CREATING");
        payment.setPaymentId(11L);
        MallCreatePaymentRequest request = new MallCreatePaymentRequest();
        request.setIdempotencyKey("request-1");
        when(stateService.beginPayment(7L, 1L, "request-1"))
                .thenReturn(new MallPaymentStateService.PaymentBegin(payment, true));
        when(paymentPort.createPayment(eq("PAY-1"), eq("ORDER-1"), any(), any()))
                .thenReturn(new PaymentPort.PaymentCreateResult(true, "PROVIDER-1", "/pay/1", "ok"));
        when(stateService.finishCreation(eq(11L), any())).thenReturn(payment);

        service.create(7L, 1L, request);

        verify(paymentPort).createPayment(eq("PAY-1"), eq("ORDER-1"), any(), any());
    }

    @Test
    void providerSuccessIsNotMarkedFailedWhenLocalPersistenceFails()
    {
        MallPayment payment = payment("CREATING");
        payment.setPaymentId(11L);
        MallCreatePaymentRequest request = new MallCreatePaymentRequest();
        request.setIdempotencyKey("request-1");
        PaymentPort.PaymentCreateResult providerResult =
                new PaymentPort.PaymentCreateResult(true, "PROVIDER-1", "/pay/1", "ok");
        when(stateService.beginPayment(7L, 1L, "request-1"))
                .thenReturn(new MallPaymentStateService.PaymentBegin(payment, true));
        when(paymentPort.createPayment(eq("PAY-1"), eq("ORDER-1"), any(), any()))
                .thenReturn(providerResult);
        when(stateService.finishCreation(11L, providerResult))
                .thenThrow(new IllegalStateException("database unavailable"));

        assertThrows(IllegalStateException.class, () -> service.create(7L, 1L, request));

        verify(stateService).beginPayment(7L, 1L, "request-1");
        verify(stateService).finishCreation(11L, providerResult);
        verifyNoMoreInteractions(stateService);
    }

    private MallPayment payment(String status)
    {
        MallPayment payment = new MallPayment();
        payment.setPaymentNo("PAY-1"); payment.setOrderNo("ORDER-1"); payment.setStatus(status);
        return payment;
    }
}
