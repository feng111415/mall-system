package com.ruoyi.mall.order.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.member.service.MallMemberTokenService;
import com.ruoyi.mall.order.domain.dto.MallCreateOrderRequest;
import com.ruoyi.mall.order.domain.dto.MallCancelOrderRequest;
import com.ruoyi.mall.order.service.MallOrderLifecycleService;
import com.ruoyi.mall.order.service.MallOrderCreateService;

@Anonymous
@RestController
@RequestMapping("/api/mall/orders")
public class MallOrderPortalController
{
    private final MallOrderCreateService orderService;
    private final MallOrderLifecycleService lifecycleService;
    private final MallMemberTokenService tokenService;

    public MallOrderPortalController(MallOrderCreateService orderService,
            MallOrderLifecycleService lifecycleService, MallMemberTokenService tokenService)
    {
        this.orderService = orderService;
        this.lifecycleService = lifecycleService;
        this.tokenService = tokenService;
    }

    @PostMapping
    public AjaxResult create(@RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody MallCreateOrderRequest request)
    {
        return AjaxResult.success("订单创建成功",
                orderService.create(tokenService.requireMemberId(authorization), request));
    }

    @PostMapping("/{orderId}/cancel")
    public AjaxResult cancel(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long orderId, @RequestBody(required = false) MallCancelOrderRequest request)
    {
        String reason = request == null ? null : request.getReason();
        return AjaxResult.success("订单已取消",
                lifecycleService.cancelByMember(tokenService.requireMemberId(authorization), orderId, reason));
    }
}
