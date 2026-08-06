package com.ruoyi.mall.member.message.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.member.message.service.MallMemberMessageService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@Anonymous
@RestController
@RequestMapping("/api/mall/member/messages")
public class MallMemberMessagePortalController
{
    private final MallMemberMessageService service;
    private final MallMemberTokenService tokenService;

    public MallMemberMessagePortalController(MallMemberMessageService service, MallMemberTokenService tokenService)
    { this.service = service; this.tokenService = tokenService; }

    @GetMapping("/summary")
    public AjaxResult summary(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    { return AjaxResult.success(service.summary(tokenService.requireMemberId(authorization))); }

    @GetMapping
    public AjaxResult list(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestParam(required = false) String category)
    { return AjaxResult.success(service.list(tokenService.requireMemberId(authorization), category)); }

    @GetMapping("/{messageId}")
    public AjaxResult detail(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long messageId)
    { return AjaxResult.success(service.detail(tokenService.requireMemberId(authorization), messageId)); }

    @PostMapping("/{messageId}/read")
    public AjaxResult read(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long messageId)
    { return AjaxResult.success(service.markRead(tokenService.requireMemberId(authorization), messageId)); }

    @PostMapping("/read-all")
    public AjaxResult readAll(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestParam(required = false) String category)
    { return AjaxResult.success(service.markAllRead(tokenService.requireMemberId(authorization), category)); }
}
