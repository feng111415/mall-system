package com.ruoyi.mall.productinventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryDetail;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryQuery;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySku;
import com.ruoyi.mall.productinventory.mapper.MallProductInventoryMapper;
import com.ruoyi.mall.productinventory.service.impl.MallProductInventoryServiceImpl;

class MallProductInventoryServiceTest
{
    @Mock private MallProductInventoryMapper mapper;
    private MallProductInventoryServiceImpl service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallProductInventoryServiceImpl(mapper);
    }

    @Test
    void normalizesFiltersBeforeQuery()
    {
        MallProductInventoryQuery query = new MallProductInventoryQuery();
        query.setProductKeyword("  台灯 ");
        query.setPublishStatus("invalid");
        query.setStockStatus("LOW_STOCK");
        when(mapper.selectSummaryList(any())).thenReturn(List.of());

        service.list(query);

        ArgumentCaptor<MallProductInventoryQuery> captor = ArgumentCaptor.forClass(MallProductInventoryQuery.class);
        verify(mapper).selectSummaryList(captor.capture());
        assertEquals("台灯", captor.getValue().getProductKeyword());
        assertEquals(null, captor.getValue().getPublishStatus());
        assertEquals("LOW_STOCK", captor.getValue().getStockStatus());
    }

    @Test
    void summarizesSkuStockWithOutOfStockPriority()
    {
        MallProductInventoryDetail detail = new MallProductInventoryDetail();
        MallProductInventorySku healthy = sku("HEALTHY", 8, 2, 1);
        MallProductInventorySku out = sku("OUT_OF_STOCK", 0, 1, 0);
        when(mapper.selectDetail(7L)).thenReturn(detail);
        when(mapper.selectSkuInventoryList(7L)).thenReturn(List.of(healthy, out));

        MallProductInventoryDetail result = service.detail(7L);

        assertEquals(2, result.getSkuCount());
        assertEquals(8, result.getAvailableQuantity());
        assertEquals(3, result.getLockedQuantity());
        assertEquals(1, result.getSoldQuantity());
        assertEquals(1, result.getOutOfStockSkuCount());
        assertEquals("OUT_OF_STOCK", result.getStockStatus());
    }

    @Test
    void rejectsInvalidDetailId()
    {
        assertThrows(RuntimeException.class, () -> service.detail(0L));
    }

    private MallProductInventorySku sku(String status, int available, int locked, int sold)
    {
        MallProductInventorySku value = new MallProductInventorySku();
        value.setStockStatus(status); value.setAvailableQuantity(available);
        value.setLockedQuantity(locked); value.setSoldQuantity(sold);
        return value;
    }
}
