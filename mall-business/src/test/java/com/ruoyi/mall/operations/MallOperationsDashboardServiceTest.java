package com.ruoyi.mall.operations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
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
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterView;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsSummary;
import com.ruoyi.mall.aftersalefunds.service.MallAfterSaleFundsCenterService;
import com.ruoyi.mall.order.operations.mapper.MallOrderOperationsMapper;
import com.ruoyi.mall.operations.domain.MallOperationsDashboardMetric;
import com.ruoyi.mall.operations.service.MallOperationsDashboardService;
import com.ruoyi.mall.productinventory.mapper.MallProductInventoryMapper;

class MallOperationsDashboardServiceTest
{
    @Mock private MallOrderOperationsMapper orderMapper;
    @Mock private MallProductInventoryMapper inventoryMapper;
    @Mock private MallAfterSaleFundsCenterService fundsService;
    private MallOperationsDashboardService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallOperationsDashboardService(orderMapper, inventoryMapper, fundsService);
        MallAfterSaleFundsSummary summary = new MallAfterSaleFundsSummary();
        summary.setPendingReview(2); summary.setRefundPending(3); summary.setRefundFailed(1);
        summary.setOpenDiffs(4); summary.setOpenAlerts(5); summary.setManualCompensations(6);
        MallAfterSaleFundsCenterView view = new MallAfterSaleFundsCenterView();
        view.setSummary(summary);
        when(fundsService.center(any(), any())).thenReturn(view);
        when(orderMapper.countOrdersSince(any())).thenReturn(8L);
        when(orderMapper.sumPaidAmountSince(any())).thenReturn(new BigDecimal("199.90"));
        when(orderMapper.countOrdersByStatus("PENDING_PAYMENT")).thenReturn(2L);
        when(orderMapper.countOrdersByStatus("PENDING_SHIPMENT")).thenReturn(1L);
        when(inventoryMapper.countLowStockSkus()).thenReturn(7L);
    }

    @Test
    void leadSeesCrossDomainMetrics()
    {
        List<String> permissions = List.of("mall:order-center:list", "mall:logistics:list", "mall:product-inventory:list",
                "mall:after-sale:list", "mall:reconciliation:list", "mall:compensation:list");
        Map<String, MallOperationsDashboardMetric> metrics = service.dashboard(permissions).getMetrics().stream()
                .collect(Collectors.toMap(MallOperationsDashboardMetric::getKey, value -> value));

        assertEquals(8L, metrics.get("todayOrders").getCount());
        assertEquals(new BigDecimal("199.90"), metrics.get("todaySales").getAmount());
        assertEquals(1L, metrics.get("pendingShipment").getCount());
        assertEquals(7L, metrics.get("lowStock").getCount());
        assertEquals(5L, metrics.get("openAlerts").getCount());
        verify(fundsService).center(any(), any());
    }

    @Test
    void customerServiceDoesNotReceiveFinanceMetrics()
    {
        List<String> permissions = List.of("mall:order-center:list", "mall:after-sale:list");
        List<String> keys = service.dashboard(permissions).getMetrics().stream()
                .map(MallOperationsDashboardMetric::getKey).collect(Collectors.toList());

        assertTrue(keys.contains("afterSalePending"));
        assertTrue(!keys.contains("openDiffs"));
        assertTrue(!keys.contains("manualCompensations"));
    }
}
