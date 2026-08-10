package com.ruoyi.mall.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.analytics.domain.MallAnalyticsMetric;
import com.ruoyi.mall.analytics.mapper.MallBusinessAnalyticsMapper;
import com.ruoyi.mall.analytics.service.MallBusinessAnalyticsService;

class MallBusinessAnalyticsServiceTest
{
    @Mock private MallBusinessAnalyticsMapper mapper;
    private MallBusinessAnalyticsService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallBusinessAnalyticsService(mapper);
        when(mapper.countOrders(any(), any())).thenReturn(12L);
        when(mapper.sumSales(any(), any())).thenReturn(new BigDecimal("1280.50"));
        when(mapper.countPaidMembers(any(), any())).thenReturn(8L);
        when(mapper.countRepeatMembers(any(), any())).thenReturn(2L);
        when(mapper.countAfterSales(any(), any())).thenReturn(3L);
        when(mapper.countAfterSalePending()).thenReturn(1L);
        when(mapper.sumRefunds(any(), any())).thenReturn(new BigDecimal("80.50"));
        when(mapper.countOpenFinancialAnomalies()).thenReturn(2L);
        when(mapper.countPendingShipment()).thenReturn(4L);
        when(mapper.countInTransitShipments()).thenReturn(5L);
        when(mapper.countDeliveredShipments(any(), any())).thenReturn(6L);
        when(mapper.countLowStockSkus()).thenReturn(7L);
        when(mapper.sumAvailableStock()).thenReturn(88L);
        when(mapper.selectSalesTrend(any(), any())).thenReturn(List.of());
        when(mapper.selectProductRanking(any(), any(), anyInt())).thenReturn(List.of());
    }

    @Test
    void operationsLeadSeesAllAggregateSections()
    {
        var view = service.analytics(30, false, List.of("mall_ops_lead"));
        assertEquals(List.of("sales", "product", "member", "fulfillment", "afterSale", "finance"), view.getSections());
        Map<String, MallAnalyticsMetric> metrics = metrics(view.getMetrics());
        assertEquals(new BigDecimal("1200.00"), metrics.get("netRevenue").getAmount());
        assertEquals("25.0", metrics.get("repeatRate").getValue());
    }

    @Test
    void productOperationsOnlySeesSalesAndProductAggregates()
    {
        var view = service.analytics(7, false, List.of("mall_product_ops"));
        assertEquals(List.of("sales", "product"), view.getSections());
        assertFalse(metrics(view.getMetrics()).containsKey("paidMemberCount"));
        assertFalse(metrics(view.getMetrics()).containsKey("refundAmount"));
        verify(mapper, never()).countPaidMembers(any(), any());
    }

    @Test
    void customerServiceOnlySeesMemberAndAfterSaleAggregates()
    {
        var view = service.analytics(90, false, List.of("mall_customer_service"));
        assertEquals(List.of("member", "afterSale"), view.getSections());
        Map<String, MallAnalyticsMetric> metrics = metrics(view.getMetrics());
        assertTrue(metrics.containsKey("paidMemberCount"));
        assertTrue(metrics.containsKey("afterSalePending"));
        assertFalse(metrics.containsKey("salesAmount"));
        verify(mapper, never()).sumSales(any(), any());
    }

    @Test
    void fulfillmentSeesOrderVolumeInventoryAndDeliveryOnly()
    {
        var view = service.analytics(30, false, List.of("mall_fulfillment"));
        assertEquals(List.of("fulfillment"), view.getSections());
        Map<String, MallAnalyticsMetric> metrics = metrics(view.getMetrics());
        assertTrue(metrics.containsKey("orderCount"));
        assertTrue(metrics.containsKey("availableStock"));
        assertTrue(metrics.containsKey("inTransit"));
        assertFalse(metrics.containsKey("salesAmount"));
    }

    @Test
    void financeSeesSalesRefundNetRevenueAndAnomalies()
    {
        var view = service.analytics(30, false, List.of("mall_finance_risk"));
        assertEquals(List.of("sales", "finance"), view.getSections());
        Map<String, MallAnalyticsMetric> metrics = metrics(view.getMetrics());
        assertEquals(new BigDecimal("80.50"), metrics.get("refundAmount").getAmount());
        assertEquals(new BigDecimal("1200.00"), metrics.get("netRevenue").getAmount());
        assertEquals(2L, metrics.get("financialAnomalies").getCount());
        assertFalse(metrics.containsKey("paidMemberCount"));
    }

    private Map<String, MallAnalyticsMetric> metrics(List<MallAnalyticsMetric> values)
    {
        return values.stream().collect(Collectors.toMap(MallAnalyticsMetric::getKey, value -> value));
    }
}
