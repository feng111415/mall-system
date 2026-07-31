package com.ruoyi.mall.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.service.MallOrderQueryService;

class MallOrderQueryServiceTest
{
    @Mock private MallOrderMapper mapper;
    private MallOrderQueryService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallOrderQueryService(mapper);
    }

    @Test
    void listNormalizesStatusAndClampsLimit()
    {
        when(mapper.selectMemberOrders(7L, "PENDING_PAYMENT", 50, 0)).thenReturn(List.of(new MallOrder()));

        assertEquals(1, service.list(7L, "pending_payment", 99, -2).size());

        verify(mapper).selectMemberOrders(7L, "PENDING_PAYMENT", 50, 0);
    }

    @Test
    void listAfterSaleUsesActiveAfterSaleQueryInsteadOfPhysicalOrderStatus()
    {
        MallOrder order = new MallOrder();
        order.setOrderId(40L);
        order.setStatus("SHIPPED");
        order.setDisplayStatus("AFTER_SALE");
        order.setLatestAfterSaleStatus("APPROVED");
        when(mapper.selectMemberAfterSaleOrders(7L, 20, 0)).thenReturn(List.of(order));

        List<MallOrder> result = service.list(7L, "AFTER_SALE", 20, 0);

        assertEquals(1, result.size());
        assertEquals("SHIPPED", result.get(0).getStatus());
        assertEquals("AFTER_SALE", result.get(0).getDisplayStatus());
        assertEquals("APPROVED", result.get(0).getLatestAfterSaleStatus());
        verify(mapper).selectMemberAfterSaleOrders(7L, 20, 0);
        verify(mapper, never()).selectMemberOrders(7L, "AFTER_SALE", 20, 0);
    }

    @Test
    void detailLoadsItemsForCurrentMemberOnly()
    {
        MallOrder order = new MallOrder(); order.setOrderId(1L);
        order.setStatus("PENDING_PAYMENT"); order.setPaymentStatus("UNPAID");
        LocalDateTime created = LocalDateTime.now(); order.setCreateTime(created);
        when(mapper.selectMemberOrder(1L, 7L)).thenReturn(order);
        when(mapper.selectMemberOrderItems(1L, 7L)).thenReturn(List.of(new MallOrderItem()));
        when(mapper.selectMemberOrderOperations(1L, 7L)).thenReturn(List.of(new MallOrderOperationLog()));

        MallOrder result = service.detail(7L, 1L);

        assertEquals(1, result.getItems().size());
        assertEquals(1, result.getOperations().size());
        assertEquals(created.plusMinutes(30), result.getPaymentCreateDeadline());
        assertEquals(created.plusMinutes(35), result.getPaymentResultDeadline());
        assertEquals(true, result.getCanCreatePayment());
        verify(mapper).selectMemberOrderItems(1L, 7L);
    }

    @Test
    void invalidStatusAndMissingOrderAreRejected()
    {
        assertThrows(ServiceException.class, () -> service.list(7L, "UNKNOWN", 20, 0));
        assertThrows(ServiceException.class, () -> service.detail(7L, 1L));
        verify(mapper, never()).selectMemberOrders(eq(7L), eq("UNKNOWN"), anyInt(), anyInt());
    }
}
