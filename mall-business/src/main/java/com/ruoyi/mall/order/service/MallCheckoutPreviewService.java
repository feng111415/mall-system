package com.ruoyi.mall.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.cart.domain.MallCartResult;
import com.ruoyi.mall.cart.service.IMallCartService;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.order.domain.MallCheckoutItem;
import com.ruoyi.mall.order.domain.MallCheckoutPreview;

@Service
public class MallCheckoutPreviewService
{
    private final IMallCartService cartService;
    private final MallMemberAuthService memberService;

    public MallCheckoutPreviewService(IMallCartService cartService, MallMemberAuthService memberService)
    {
        this.cartService = cartService;
        this.memberService = memberService;
    }

    public MallCheckoutPreview preview(Long memberId)
    {
        MallCartResult cart = cartService.selectCart(memberId);
        List<MallMemberAddress> addresses = memberService.addresses(memberId);
        List<MallCheckoutItem> items = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        BigDecimal productAmount = BigDecimal.ZERO;
        for (MallCartItem item : cart.getItems())
        {
            if (!"1".equals(item.getSelectedFlag())) continue;
            MallCheckoutItem checkoutItem = new MallCheckoutItem();
            checkoutItem.setSkuId(item.getSkuId()); checkoutItem.setSkuCode(item.getSkuCode());
            checkoutItem.setProductName(item.getProductName()); checkoutItem.setSkuName(item.getSkuName());
            checkoutItem.setProductImage(item.getProductImage()); checkoutItem.setUnitPrice(item.getPrice());
            checkoutItem.setQuantity(item.getQuantity()); checkoutItem.setLineAmount(item.getLineAmount());
            checkoutItem.setAvailableStock(item.getAvailableStock()); items.add(checkoutItem);
            productAmount = productAmount.add(item.getLineAmount() == null ? BigDecimal.ZERO : item.getLineAmount());
            if (!Boolean.TRUE.equals(item.getValid())) messages.add(item.getProductName() + " 已失效");
            else if (Boolean.TRUE.equals(item.getStockShortage())) messages.add(item.getProductName() + " 库存不足，仅剩 " + item.getAvailableStock() + " 件");
        }
        if (items.isEmpty()) messages.add("请至少选择一件有效商品");
        if (!Boolean.TRUE.equals(cart.getCanCheckout()) && messages.isEmpty()) messages.add("购物车商品状态已变化，请重新确认");
        if (addresses == null || addresses.isEmpty()) messages.add("请先添加收货地址");
        Long defaultAddressId = addresses == null ? null : addresses.stream()
                .filter(address -> "1".equals(address.getIsDefault())).map(MallMemberAddress::getAddressId)
                .findFirst().orElse(addresses.isEmpty() ? null : addresses.get(0).getAddressId());
        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        MallCheckoutPreview preview = new MallCheckoutPreview();
        preview.setItems(items); preview.setAddresses(addresses == null ? List.of() : addresses);
        preview.setDefaultAddressId(defaultAddressId); preview.setProductAmount(productAmount);
        preview.setShippingFee(shippingFee); preview.setDiscountAmount(discountAmount);
        preview.setPayableAmount(productAmount.add(shippingFee).subtract(discountAmount));
        preview.setCanSubmit(messages.isEmpty()); preview.setValidationMessages(messages);
        return preview;
    }
}
