package com.ruoyi.mall.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.governance.service.MallCompensationService;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.service.MallPaymentResultPreparationService;
import com.ruoyi.mall.payment.service.MallPaymentStateService;

class MallPaymentResultPreparationServiceTest
{
    @Mock private MallPaymentStateService stateService;
    @Mock private MallCompensationService compensationService;
    private MallPaymentResultPreparationService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallPaymentResultPreparationService(stateService, compensationService);
    }

    @Test
    void createsCompensationTaskForLatePaymentInPreparationTransaction()
    {
        MallPayment payment = payment("REFUNDING");
        when(stateService.mockSuccess(7L, "PAY-1")).thenReturn(payment);

        MallPayment result = service.recordMockSuccess(7L, "PAY-1");

        assertEquals("REFUNDING", result.getStatus());
        verify(compensationService).create("LATE_PAYMENT_REFUND", "PAY-1", "ORDER-1",
                "订单关闭后收到支付成功结果");
    }

    @Test
    void doesNotCreateCompensationTaskForOnTimePayment()
    {
        MallPayment payment = payment("SUCCESS");
        when(stateService.mockSuccess(7L, "PAY-1")).thenReturn(payment);

        service.recordMockSuccess(7L, "PAY-1");

        verify(compensationService, never()).create(
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void propagatesTaskPersistenceFailureSoTransactionCanRollBack()
    {
        MallPayment payment = payment("REFUNDING");
        when(stateService.mockSuccess(7L, "PAY-1")).thenReturn(payment);
        when(compensationService.create("LATE_PAYMENT_REFUND", "PAY-1", "ORDER-1",
                "订单关闭后收到支付成功结果")).thenThrow(new IllegalStateException("task failed"));

        assertThrows(IllegalStateException.class, () -> service.recordMockSuccess(7L, "PAY-1"));
    }

    private MallPayment payment(String status)
    {
        MallPayment payment = new MallPayment();
        payment.setPaymentNo("PAY-1"); payment.setOrderNo("ORDER-1"); payment.setStatus(status);
        return payment;
    }
}
