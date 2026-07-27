package com.ruoyi.mall.aftersale.web;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.aftersale.domain.dto.MallRefundRequest;
import com.ruoyi.mall.aftersale.service.MallRefundService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

@Anonymous
@RestController
@RequestMapping("/api/mall")
public class MallRefundPortalController
{
    private final MallRefundService refundService;
    private final MallMemberTokenService tokenService;

    public MallRefundPortalController(MallRefundService refundService, MallMemberTokenService tokenService)
    {
        this.refundService = refundService;
        this.tokenService = tokenService;
    }

    @PostMapping("/orders/{orderId}/refund")
    public AjaxResult apply(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId, @Valid @RequestBody MallRefundRequest request)
    {
        return AjaxResult.success("退款申请已提交", refundService.apply(
                tokenService.requireMemberId(authorization), orderId, request.getReason()));
    }

    @GetMapping("/refunds")
    public AjaxResult list(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        return AjaxResult.success(refundService.list(tokenService.requireMemberId(authorization)));
    }

    @GetMapping("/refunds/{refundId}")
    public AjaxResult detail(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long refundId)
    {
        return AjaxResult.success(refundService.detail(tokenService.requireMemberId(authorization), refundId));
    }
}
