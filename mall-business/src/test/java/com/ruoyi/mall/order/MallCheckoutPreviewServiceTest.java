package com.ruoyi.mall.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.cart.domain.MallCartResult;
import com.ruoyi.mall.cart.service.IMallCartService;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.order.service.MallCheckoutPreviewService;

class MallCheckoutPreviewServiceTest
{
    @Mock private IMallCartService cartService;
    @Mock private MallMemberAuthService memberService;
    private MallCheckoutPreviewService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallCheckoutPreviewService(cartService, memberService);
    }

    @Test
    void validSelectedItemAndAddressProducePayablePreview()
    {
        MallCartItem item = new MallCartItem();
        item.setSkuId(10L); item.setQuantity(2); item.setPrice(new BigDecimal("12.50"));
        item.setLineAmount(new BigDecimal("25.00")); item.setSelectedFlag("1");
        item.setValid(true); item.setStockShortage(false);
        MallCartResult cart = new MallCartResult(); cart.setItems(List.of(item)); cart.setCanCheckout(true);
        MallMemberAddress address = new MallMemberAddress(); address.setAddressId(7L); address.setIsDefault("1");
        when(cartService.selectCart(1L)).thenReturn(cart);
        when(memberService.addresses(1L)).thenReturn(List.of(address));

        var preview = service.preview(1L);

        assertEquals(new BigDecimal("25.00"), preview.getPayableAmount());
        assertEquals(7L, preview.getDefaultAddressId());
        assertTrue(preview.getCanSubmit());
    }

    @Test
    void emptySelectionCannotSubmit()
    {
        MallCartResult cart = new MallCartResult(); cart.setItems(List.of()); cart.setCanCheckout(false);
        when(cartService.selectCart(1L)).thenReturn(cart);
        when(memberService.addresses(1L)).thenReturn(List.of());

        var preview = service.preview(1L);

        assertTrue(!preview.getCanSubmit());
        assertTrue(preview.getValidationMessages().contains("请至少选择一件有效商品"));
    }

    @Test
    void stockShortageBlocksSubmitWithoutChangingQuantity()
    {
        MallCartItem item = new MallCartItem();
        item.setSkuId(10L); item.setQuantity(4); item.setPrice(new BigDecimal("5.00"));
        item.setLineAmount(new BigDecimal("20.00")); item.setSelectedFlag("1");
        item.setValid(true); item.setStockShortage(true); item.setAvailableStock(2); item.setProductName("库存商品");
        MallCartResult cart = new MallCartResult(); cart.setItems(List.of(item)); cart.setCanCheckout(false);
        MallMemberAddress address = new MallMemberAddress(); address.setAddressId(7L); address.setIsDefault("1");
        when(cartService.selectCart(1L)).thenReturn(cart);
        when(memberService.addresses(1L)).thenReturn(List.of(address));

        var preview = service.preview(1L);

        assertTrue(!preview.getCanSubmit());
        assertTrue(preview.getValidationMessages().get(0).contains("库存不足"));
        assertEquals(4, preview.getItems().get(0).getQuantity());
    }
}
