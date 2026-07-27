package com.ruoyi.mall.order.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.member.service.MallMemberTokenService;
import com.ruoyi.mall.order.service.MallCheckoutPreviewService;

@Anonymous
@RestController
@RequestMapping("/api/mall/checkout")
public class MallCheckoutPortalController
{
    private final MallCheckoutPreviewService service;
    private final MallMemberTokenService tokenService;

    public MallCheckoutPortalController(MallCheckoutPreviewService service, MallMemberTokenService tokenService)
    {
        this.service = service;
        this.tokenService = tokenService;
    }

    @GetMapping("/preview")
    public AjaxResult preview(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        return AjaxResult.success(service.preview(tokenService.requireMemberId(authorization)));
    }
}
