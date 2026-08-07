package com.ruoyi.mall.coupon.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.coupon.service.MallCouponService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@Anonymous
@RestController
@RequestMapping("/api/mall/coupons")
public class MallCouponPortalController
{
    private final MallCouponService service;
    private final MallMemberTokenService tokenService;

    public MallCouponPortalController(MallCouponService service, MallMemberTokenService tokenService)
    { this.service = service; this.tokenService = tokenService; }

    @GetMapping
    public AjaxResult list(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.memberCoupons(tokenService.requireMemberId(authorization))); }

    @GetMapping("/claimable")
    public AjaxResult claimable(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.claimable(tokenService.requireMemberId(authorization))); }

    @PostMapping("/{couponId}/claim")
    public AjaxResult claim(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long couponId)
    { return AjaxResult.success("优惠券领取成功", service.claim(tokenService.requireMemberId(authorization), couponId)); }
}
