package com.ruoyi.mall.order.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsDetail;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsQuery;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsSummary;
import com.ruoyi.mall.order.operations.mapper.MallOrderOperationsMapper;
import com.ruoyi.mall.order.operations.service.MallOrderOperationsService;

class MallOrderOperationsServiceTest
{
    @Mock private MallOrderOperationsMapper mapper;
    private MallOrderOperationsService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallOrderOperationsService(mapper);
    }

    @Test
    void listNormalizesFiltersBeforeQuery()
    {
        MallOrderOperationsQuery query = new MallOrderOperationsQuery();
        query.setOrderNo("  M-1  "); query.setStatus(" shipped "); query.setPaymentStatus(" paid ");
        query.setRiskStatus(" review "); query.setLogisticsStatus(" delivered "); query.setAfterSaleStatus(" none ");
        when(mapper.selectOrders(query)).thenReturn(List.of(new MallOrderOperationsSummary()));

        assertEquals(1, service.list(query).size());
        assertEquals("M-1", query.getOrderNo());
        assertEquals("SHIPPED", query.getStatus());
        assertEquals("REVIEW", query.getRiskStatus());
        assertEquals("NONE", query.getAfterSaleStatus());
        verify(mapper).selectOrders(query);
    }

    @Test
    void detailLoadsEveryOperationsDomain()
    {
        MallOrderOperationsDetail detail = new MallOrderOperationsDetail();
        detail.setOrderNo("M-1");
        when(mapper.selectOrderDetail(1L)).thenReturn(detail);
        when(mapper.selectItems(1L)).thenReturn(List.of());
        when(mapper.selectPayments(1L)).thenReturn(List.of());
        when(mapper.selectReservations("M-1")).thenReturn(List.of());
        when(mapper.selectAfterSales(1L)).thenReturn(List.of());
        when(mapper.selectRiskRecords(1L)).thenReturn(List.of());
        when(mapper.selectOperations(1L)).thenReturn(List.of());

        assertEquals(detail, service.detail(1L));
        verify(mapper).selectShipment(1L);
        verify(mapper).selectAfterSales(1L);
    }

    @Test
    void invalidFilterAndOrderAreRejected()
    {
        MallOrderOperationsQuery query = new MallOrderOperationsQuery();
        query.setStatus("not-a-status");
        assertThrows(ServiceException.class, () -> service.list(query));
        assertThrows(ServiceException.class, () -> service.detail(0L));
    }

    @Test
    void invalidRiskStatusAndReversedDateRangeAreRejected()
    {
        MallOrderOperationsQuery riskQuery = new MallOrderOperationsQuery();
        riskQuery.setRiskStatus("MANUAL_REVIEW");
        assertThrows(ServiceException.class, () -> service.list(riskQuery));

        MallOrderOperationsQuery dateQuery = new MallOrderOperationsQuery();
        dateQuery.setCreateStart(LocalDateTime.of(2026, 8, 4, 12, 0));
        dateQuery.setCreateEnd(LocalDateTime.of(2026, 8, 4, 11, 59));
        assertThrows(ServiceException.class, () -> service.list(dateQuery));
    }
}
