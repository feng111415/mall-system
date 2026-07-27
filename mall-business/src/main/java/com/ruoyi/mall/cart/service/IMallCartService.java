package com.ruoyi.mall.cart.service;

import com.ruoyi.mall.cart.domain.MallCartResult;
import com.ruoyi.mall.cart.domain.dto.MallCartAddRequest;

public interface IMallCartService
{
    MallCartResult selectCart(Long memberId);
    MallCartResult add(Long memberId, MallCartAddRequest request);
    MallCartResult updateQuantity(Long memberId, Long skuId, int quantity);
    MallCartResult updateSelected(Long memberId, Long skuId, boolean selected);
    MallCartResult remove(Long memberId, Long skuId);
}
