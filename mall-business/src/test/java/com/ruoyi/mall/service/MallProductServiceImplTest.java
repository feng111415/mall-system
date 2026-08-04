package com.ruoyi.mall.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.domain.product.MallSku;
import com.ruoyi.mall.domain.product.MallSpu;
import com.ruoyi.mall.domain.product.dto.MallCatalogProductQuery;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.impl.MallProductServiceImpl;

class MallProductServiceImplTest
{
    @Mock
    private MallProductMapper mapper;
    private MallProductServiceImpl service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallProductServiceImpl(mapper);
    }

    @Test
    void rejectsNegativePriceBeforeWriting()
    {
        MallSpu spu = productWith(new BigDecimal("-0.01"), 1);
        assertThrows(ServiceException.class, () -> service.saveProduct(spu));
        verify(mapper, never()).insertSpu(any());
    }

    @Test
    void rejectsNegativeAvailableStockBeforeWriting()
    {
        MallSpu spu = productWith(new BigDecimal("9.90"), -1);
        assertThrows(ServiceException.class, () -> service.saveProduct(spu));
        verify(mapper, never()).insertSpu(any());
    }

    @Test
    void savesSpuAndSkuTogether()
    {
        MallSpu spu = productWith(new BigDecimal("9.90"), 3);
        service.saveProduct(spu);
        verify(mapper).insertSpu(spu);
        verify(mapper).insertSku(spu.getSkuList().get(0));
    }

    @Test
    void normalizesCatalogQueryBeforePassingItToMapper()
    {
        MallCatalogProductQuery query = new MallCatalogProductQuery();
        query.setKeyword("  耳机  ");
        query.setSort("untrusted-column");

        service.selectPublishedProducts(query);

        ArgumentCaptor<MallCatalogProductQuery> captor = ArgumentCaptor.forClass(MallCatalogProductQuery.class);
        verify(mapper).selectPublishedSpuList(captor.capture());
        assertEquals("耳机", captor.getValue().getKeyword());
        assertEquals("default", captor.getValue().getSort());
    }

    @Test
    void updatesExistingSkuWithoutRebuildingItsIdentity()
    {
        MallSpu spu = productWith(new BigDecimal("9.90"), 3);
        spu.setSpuId(20L);
        MallSku existing = spu.getSkuList().get(0);
        existing.setSkuId(10L);
        when(mapper.selectSkuListBySpuId(20L)).thenReturn(List.of(existing));
        when(mapper.updateSku(existing)).thenReturn(1);

        service.saveProduct(spu);

        verify(mapper).updateSku(existing);
        verify(mapper, never()).deleteSku(10L);
    }

    @Test
    void refusesRemovingSkuWithOperationalData()
    {
        MallSpu spu = productWith(new BigDecimal("9.90"), 3);
        spu.setSpuId(20L);
        MallSku existing = spu.getSkuList().get(0);
        existing.setSkuId(10L);
        MallSku retained = new MallSku();
        retained.setSkuId(11L); retained.setSkuCode("SKU-TEST-2"); retained.setPrice(new BigDecimal("10.90")); retained.setAvailableStock(1); retained.setStatus("1"); retained.setSpuId(20L);
        spu.setSkuList(List.of(retained));
        when(mapper.selectSkuListBySpuId(20L)).thenReturn(List.of(existing, retained));
        when(mapper.countSkuOperationalData(10L)).thenReturn(1);
        when(mapper.updateSku(retained)).thenReturn(1);

        assertThrows(ServiceException.class, () -> service.saveProduct(spu));
        verify(mapper, never()).deleteSku(10L);
    }

    private MallSpu productWith(BigDecimal price, int stock)
    {
        MallSku sku = new MallSku();
        sku.setSkuCode("SKU-TEST-1");
        sku.setPrice(price);
        sku.setAvailableStock(stock);
        MallSpu spu = new MallSpu();
        spu.setSpuCode("SPU-TEST-1");
        spu.setProductName("测试商品");
        spu.setCategoryId(1L);
        spu.setSkuList(List.of(sku));
        return spu;
    }
}
