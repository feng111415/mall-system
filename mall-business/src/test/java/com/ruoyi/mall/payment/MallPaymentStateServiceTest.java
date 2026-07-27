package com.ruoyi.mall.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.application.port.PaymentPort.PaymentCreateResult;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.payment.service.MallPaymentStateService;

class MallPaymentStateServiceTest
{
    @Mock private MallPaymentMapper paymentMapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private InventoryPort inventoryPort;
    private MallPaymentStateService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallPaymentStateService(paymentMapper, orderMapper, inventoryPort);
    }

    @Test
    void beginPaymentCreatesOneCreatingRecord()
    {
        MallOrder order = order("PENDING_PAYMENT", "UNPAID");
        when(paymentMapper.selectByOrderIdempotency(7L, 1L, "pay-1")).thenReturn(null);
        when(orderMapper.selectByIdForUpdate(1L, 7L)).thenReturn(order);
        when(paymentMapper.insertPayment(any())).thenAnswer(invocation -> {
            MallPayment payment = invocation.getArgument(0); payment.setPaymentId(11L); return 1;
        });

        MallPaymentStateService.PaymentBegin begin = service.beginPayment(7L, 1L, "pay-1");

        assertEquals(true, begin.shouldCallProvider());
        assertEquals("CREATING", begin.payment().getStatus());
        assertEquals(new BigDecimal("12.50"), begin.payment().getAmount());
    }

    @Test
    void successfulProviderResultMovesOrderToPaying()
    {
        MallPayment payment = payment("CREATING"); payment.setPaymentId(11L);
        when(paymentMapper.selectByIdForUpdate(11L)).thenReturn(payment);
        when(orderMapper.selectByIdForUpdate(1L, 7L)).thenReturn(order("PENDING_PAYMENT", "UNPAID"));
        when(orderMapper.markPaymentPaying(1L)).thenReturn(1);
        when(paymentMapper.updateCreationResult(11L, "PAYING", "PROVIDER-1", "/pay/1", null)).thenReturn(1);

        MallPayment result = service.finishCreation(11L, new PaymentCreateResult(true, "PROVIDER-1", "/pay/1", "ok"));

        assertEquals("PAYING", result.getStatus());
        verify(orderMapper).markPaymentPaying(1L);
    }

    @Test
    void mockSuccessConfirmsOrderAndInventoryExactlyOnce()
    {
        MallPayment payment = payment("PAYING"); payment.setPaymentId(11L);
        MallOrder order = order("PENDING_PAYMENT", "PAYING");
        when(paymentMapper.selectByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        when(orderMapper.selectByIdForUpdate(1L, 7L)).thenReturn(order);
        when(orderMapper.markPaymentSuccess(1L)).thenReturn(1);
        when(paymentMapper.updateSuccess(11L)).thenReturn(1);
        when(orderMapper.insertOperationLog(any())).thenReturn(1);

        MallPayment result = service.mockSuccess(7L, "PAY-1");

        assertEquals("SUCCESS", result.getStatus());
        verify(inventoryPort).confirm("M20260727150000000001", true);
    }

    @Test
    void repeatedSuccessReturnsWithoutConfirmingAgain()
    {
        MallPayment payment = payment("SUCCESS");
        when(paymentMapper.selectByPaymentNoForUpdate("PAY-1")).thenReturn(payment);

        MallPayment result = service.mockSuccess(7L, "PAY-1");

        assertEquals("SUCCESS", result.getStatus());
        verify(inventoryPort, never()).confirm(any(), any(Boolean.class));
        verify(orderMapper, never()).markPaymentSuccess(anyLong());
    }

    @Test
    void cannotPayClosedOrder()
    {
        MallPayment payment = payment("PAYING");
        when(paymentMapper.selectByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        when(orderMapper.selectByIdForUpdate(1L, 7L)).thenReturn(order("CLOSED", "UNPAID"));

        assertThrows(ServiceException.class, () -> service.mockSuccess(7L, "PAY-1"));
        verify(inventoryPort, never()).confirm(any(), any(Boolean.class));
    }

    private MallOrder order(String status, String paymentStatus)
    {
        MallOrder order = new MallOrder();
        order.setOrderId(1L); order.setOrderNo("M20260727150000000001"); order.setMemberId(7L);
        order.setStatus(status); order.setPaymentStatus(paymentStatus); order.setPayableAmount(new BigDecimal("12.50"));
        order.setVersion(0); order.setCreateTime(LocalDateTime.now());
        return order;
    }

    private MallPayment payment(String status)
    {
        MallPayment payment = new MallPayment();
        payment.setPaymentId(11L); payment.setPaymentNo("PAY-1"); payment.setOrderId(1L);
        payment.setOrderNo("M20260727150000000001"); payment.setMemberId(7L);
        payment.setAmount(new BigDecimal("12.50")); payment.setStatus(status);
        return payment;
    }
}
