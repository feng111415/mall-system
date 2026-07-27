package com.ruoyi.mall.cart.web;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.cart.domain.dto.MallCartAddRequest;
import com.ruoyi.mall.cart.domain.dto.MallCartQuantityRequest;
import com.ruoyi.mall.cart.domain.dto.MallCartSelectedRequest;
import com.ruoyi.mall.cart.service.IMallCartService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@Anonymous
@RestController
@RequestMapping("/api/mall/cart")
public class MallCartPortalController
{
    private final IMallCartService cartService;
    private final MallMemberTokenService tokenService;

    public MallCartPortalController(IMallCartService cartService, MallMemberTokenService tokenService)
    {
        this.cartService = cartService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public AjaxResult cart(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        return AjaxResult.success(cartService.selectCart(memberId(authorization)));
    }

    @PostMapping("/items")
    public AjaxResult add(@RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody MallCartAddRequest request)
    {
        return AjaxResult.success("已加入购物车", cartService.add(memberId(authorization), request));
    }

    @PutMapping("/items/{skuId}")
    public AjaxResult quantity(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long skuId, @RequestBody MallCartQuantityRequest request)
    {
        return AjaxResult.success(cartService.updateQuantity(memberId(authorization), skuId,
                request == null || request.getQuantity() == null ? 0 : request.getQuantity()));
    }

    @PutMapping("/items/{skuId}/selected")
    public AjaxResult selected(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long skuId, @RequestBody MallCartSelectedRequest request)
    {
        boolean selected = request != null && Boolean.TRUE.equals(request.getSelected());
        return AjaxResult.success(cartService.updateSelected(memberId(authorization), skuId, selected));
    }

    @DeleteMapping("/items/{skuId}")
    public AjaxResult remove(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long skuId)
    {
        return AjaxResult.success("已移除购物车商品", cartService.remove(memberId(authorization), skuId));
    }

    private Long memberId(String authorization) { return tokenService.requireMemberId(authorization); }
}
