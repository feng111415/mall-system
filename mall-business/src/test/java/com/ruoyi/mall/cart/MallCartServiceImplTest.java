package com.ruoyi.mall.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.cart.domain.dto.MallCartAddRequest;
import com.ruoyi.mall.cart.mapper.MallCartMapper;
import com.ruoyi.mall.cart.service.impl.MallCartServiceImpl;

class MallCartServiceImplTest
{
    @Mock private MallCartMapper mapper;
    private MallCartServiceImpl service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallCartServiceImpl(mapper);
        when(mapper.selectItems(anyLong())).thenReturn(List.of());
    }

    @Test
    void rejectsZeroQuantityBeforeReadingSku()
    {
        MallCartAddRequest request = new MallCartAddRequest(); request.setSkuId(10L); request.setQuantity(0);
        assertThrows(ServiceException.class, () -> service.add(1L, request));
        verify(mapper, never()).selectSkuSnapshot(10L);
    }

    @Test
    void newItemIsInsertedWithSelectedFlag()
    {
        when(mapper.selectSkuSnapshot(10L)).thenReturn(sku(5));
        when(mapper.selectItemForUpdate(1L, 10L)).thenReturn(null);
        when(mapper.insertItemIgnore(any())).thenReturn(1);
        MallCartAddRequest request = new MallCartAddRequest(); request.setSkuId(10L); request.setQuantity(2);

        service.add(1L, request);

        verify(mapper).insertItemIgnore(any());
        verify(mapper, never()).updateQuantity(1L, 10L, 2);
    }

    @Test
    void repeatedAddAccumulatesQuantity()
    {
        when(mapper.selectSkuSnapshot(10L)).thenReturn(sku(5));
        MallCartItem existing = sku(5); existing.setQuantity(2);
        when(mapper.selectItemForUpdate(1L, 10L)).thenReturn(existing);
        when(mapper.updateQuantity(1L, 10L, 4)).thenReturn(1);
        MallCartAddRequest request = new MallCartAddRequest(); request.setSkuId(10L); request.setQuantity(2);

        service.add(1L, request);

        verify(mapper).updateQuantity(1L, 10L, 4);
        verify(mapper, never()).insertItemIgnore(any());
    }

    @Test
    void addingOverCurrentStockIsRejected()
    {
        when(mapper.selectSkuSnapshot(10L)).thenReturn(sku(2));
        MallCartItem existing = sku(2); existing.setQuantity(1);
        when(mapper.selectItemForUpdate(1L, 10L)).thenReturn(existing);
        MallCartAddRequest request = new MallCartAddRequest(); request.setSkuId(10L); request.setQuantity(2);

        assertThrows(ServiceException.class, () -> service.add(1L, request));
        verify(mapper, never()).updateQuantity(1L, 10L, 3);
    }

    @Test
    void soldOutItemIsKeptButDeselected()
    {
        MallCartItem item = sku(0); item.setSelectedFlag("1"); item.setQuantity(1);
        when(mapper.selectItems(1L)).thenReturn(List.of(item));

        var result = service.selectCart(1L);

        assertEquals("0", result.getItems().get(0).getSelectedFlag());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        assertEquals(false, result.getCanCheckout());
        verify(mapper).updateSelected(1L, 10L, "0");
    }

    private MallCartItem sku(int stock)
    {
        MallCartItem item = new MallCartItem();
        item.setSkuId(10L); item.setSkuStatus("1"); item.setSkuDelFlag("0"); item.setSpuPublishStatus("1"); item.setSpuDelFlag("0");
        item.setAvailableStock(stock); item.setPrice(new BigDecimal("12.50")); item.setQuantity(1); item.setSelectedFlag("1");
        item.setProductName("测试商品");
        return item;
    }
}
