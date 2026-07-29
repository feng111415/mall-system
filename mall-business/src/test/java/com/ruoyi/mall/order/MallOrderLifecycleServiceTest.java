package com.ruoyi.mall.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.application.port.PaymentExpirationPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.service.MallOrderLifecycleService;

class MallOrderLifecycleServiceTest
{
    @Mock private MallOrderMapper mapper;
    @Mock private InventoryPort inventoryPort;
    @Mock private PaymentExpirationPort paymentExpirationPort;
    private MallOrderLifecycleService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallOrderLifecycleService(mapper, inventoryPort, paymentExpirationPort);
        when(mapper.insertOperationLog(any())).thenReturn(1);
    }

    @Test
    void memberCancellationReleasesLockedStockAndWritesLog()
    {
        MallOrder order = order("PENDING_PAYMENT", "UNPAID");
        when(mapper.selectByIdForUpdate(1L, 7L)).thenReturn(order);
        when(mapper.updateStatus(1L, "PENDING_PAYMENT", "CANCELLED", "用户改变主意")).thenReturn(1);

        MallOrder result = service.cancelByMember(7L, 1L, "用户改变主意");

        assertEquals("CANCELLED", result.getStatus());
        verify(inventoryPort).release("M20260727150000000001");
        verify(mapper).insertOperationLog(any());
    }

    @Test
    void paidOrShippedOrderCannotBeCancelled()
    {
        MallOrder order = order("PENDING_SHIPMENT", "PAID");
        when(mapper.selectByIdForUpdate(1L, 7L)).thenReturn(order);

        assertThrows(ServiceException.class, () -> service.cancelByMember(7L, 1L, null));

        verify(mapper, never()).updateStatus(any(), any(), any(), any());
        verify(inventoryPort, never()).release(any());
    }

    @Test
    void timeoutClosesOnlyExpiredUnpaidOrders()
    {
        MallOrder candidate = order("PENDING_PAYMENT", "UNPAID");
        candidate.setCreateTime(LocalDateTime.now().minusMinutes(31));
        when(mapper.selectTimeoutCandidates(any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(candidate));
        when(mapper.selectByIdForUpdate(1L, null)).thenReturn(candidate);
        when(mapper.updateStatus(eq(1L), eq("PENDING_PAYMENT"), eq("CLOSED"), eq("支付超时自动关闭"))).thenReturn(1);

        assertEquals(1, service.closeExpiredOrders());

        verify(inventoryPort).release("M20260727150000000001");
        verify(mapper).insertOperationLog(any());
    }

    @Test
    void paymentInResultWaitPeriodStaysOpen()
    {
        MallOrder candidate = order("PENDING_PAYMENT", "PAYING");
        candidate.setCreateTime(LocalDateTime.now().minusMinutes(31));
        when(mapper.selectTimeoutCandidates(any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(candidate));

        assertEquals(0, service.closeExpiredOrders());

        verify(paymentExpirationPort, never()).closePendingPayment(any());
        verify(inventoryPort, never()).release(any());
    }

    @Test
    void paymentPastResultDeadlineClosesAndReleasesStock()
    {
        MallOrder candidate = order("PENDING_PAYMENT", "PAYING");
        candidate.setCreateTime(LocalDateTime.now().minusMinutes(36));
        when(mapper.selectTimeoutCandidates(any(LocalDateTime.class), any(LocalDateTime.class), anyInt()))
                .thenReturn(List.of(candidate));
        when(paymentExpirationPort.closePendingPayment(1L)).thenReturn(true);
        when(mapper.selectByIdForUpdate(1L, null)).thenReturn(candidate);
        when(mapper.closeExpired(1L, "PAYING", "支付结果等待超时自动关闭")).thenReturn(1);

        assertEquals(1, service.closeExpiredOrders());

        verify(paymentExpirationPort).closePendingPayment(1L);
        verify(inventoryPort).release("M20260727150000000001");
        verify(mapper).insertOperationLog(any());
    }

    private MallOrder order(String status, String paymentStatus)
    {
        MallOrder order = new MallOrder();
        order.setOrderId(1L); order.setOrderNo("M20260727150000000001"); order.setMemberId(7L);
        order.setStatus(status); order.setPaymentStatus(paymentStatus); order.setVersion(0);
        order.setCreateTime(LocalDateTime.now().minusMinutes(1));
        return order;
    }
}
