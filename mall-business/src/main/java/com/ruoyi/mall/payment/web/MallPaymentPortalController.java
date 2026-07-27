package com.ruoyi.mall.payment.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.member.service.MallMemberTokenService;
import com.ruoyi.mall.payment.domain.dto.MallCreatePaymentRequest;
import com.ruoyi.mall.payment.service.MallPaymentService;

@Anonymous
@RestController
@RequestMapping("/api/mall")
public class MallPaymentPortalController
{
    private final MallPaymentService paymentService;
    private final MallMemberTokenService tokenService;

    public MallPaymentPortalController(MallPaymentService paymentService, MallMemberTokenService tokenService)
    {
        this.paymentService = paymentService;
        this.tokenService = tokenService;
    }

    @PostMapping("/orders/{orderId}/payment")
    public AjaxResult create(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId, @RequestBody MallCreatePaymentRequest request)
    {
        return AjaxResult.success("支付单创建成功", paymentService.create(
                tokenService.requireMemberId(authorization), orderId, request));
    }

    @PostMapping("/payments/{paymentNo}/mock-success")
    public AjaxResult mockSuccess(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable String paymentNo)
    {
        return AjaxResult.success("模拟支付成功", paymentService.mockSuccess(
                tokenService.requireMemberId(authorization), paymentNo));
    }
}
