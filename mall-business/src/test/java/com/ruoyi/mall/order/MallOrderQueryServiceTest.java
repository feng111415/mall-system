package com.ruoyi.mall.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
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
    void detailLoadsItemsForCurrentMemberOnly()
    {
        MallOrder order = new MallOrder(); order.setOrderId(1L);
        when(mapper.selectMemberOrder(1L, 7L)).thenReturn(order);
        when(mapper.selectMemberOrderItems(1L, 7L)).thenReturn(List.of(new MallOrderItem()));

        MallOrder result = service.detail(7L, 1L);

        assertEquals(1, result.getItems().size());
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
