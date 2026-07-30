package com.ruoyi.mall.aftersale.item.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.aftersale.item.domain.dto.MallItemAfterSaleRequest;
import com.ruoyi.mall.aftersale.item.domain.dto.MallReturnTrackingRequest;
import com.ruoyi.mall.aftersale.item.service.MallItemAfterSaleService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@Anonymous
@RestController
@RequestMapping("/api/mall")
public class MallItemAfterSalePortalController
{
    private final MallItemAfterSaleService service;
    private final MallMemberTokenService tokenService;
    public MallItemAfterSalePortalController(MallItemAfterSaleService service, MallMemberTokenService tokenService) { this.service = service; this.tokenService = tokenService; }

    @PostMapping("/orders/{orderId}/after-sales")
    public AjaxResult apply(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long orderId, @Valid @RequestBody MallItemAfterSaleRequest request)
    { return AjaxResult.success(service.apply(tokenService.requireMemberId(authorization), orderId, request)); }

    @GetMapping("/after-sales")
    public AjaxResult list(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.list(tokenService.requireMemberId(authorization))); }

    @GetMapping("/after-sales/{afterSaleId}")
    public AjaxResult detail(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization, @PathVariable Long afterSaleId)
    { return AjaxResult.success(service.detail(tokenService.requireMemberId(authorization), afterSaleId)); }

    @PostMapping("/after-sales/{afterSaleId}/return-tracking")
    public AjaxResult returnTracking(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long afterSaleId, @Valid @RequestBody MallReturnTrackingRequest request)
    { return AjaxResult.success(service.submitReturnTracking(tokenService.requireMemberId(authorization), afterSaleId, request)); }
}
