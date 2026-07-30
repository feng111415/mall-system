package com.ruoyi.mall.order.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
import com.ruoyi.mall.order.service.MallOrderQueryService;
import com.ruoyi.mall.order.service.MallOrderCreateService;
import com.ruoyi.mall.logistics.service.MallLogisticsService;

@Anonymous
@RestController
@RequestMapping("/api/mall/orders")
public class MallOrderPortalController
{
    private final MallOrderCreateService orderService;
    private final MallOrderLifecycleService lifecycleService;
    private final MallOrderQueryService queryService;
    private final MallMemberTokenService tokenService;
    private final MallLogisticsService logisticsService;

    public MallOrderPortalController(MallOrderCreateService orderService,
            MallOrderLifecycleService lifecycleService, MallOrderQueryService queryService,
            MallMemberTokenService tokenService, MallLogisticsService logisticsService)
    {
        this.orderService = orderService;
        this.lifecycleService = lifecycleService;
        this.queryService = queryService;
        this.tokenService = tokenService;
        this.logisticsService = logisticsService;
    }

    @GetMapping
    public AjaxResult list(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset)
    {
        return AjaxResult.success(queryService.list(tokenService.requireMemberId(authorization), status, limit, offset));
    }

    @GetMapping("/{orderId}")
    public AjaxResult detail(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long orderId)
    {
        return AjaxResult.success(queryService.detail(tokenService.requireMemberId(authorization), orderId));
    }

    @PostMapping
    public AjaxResult create(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestBody MallCreateOrderRequest request)
    {
        return AjaxResult.success("订单创建成功",
                orderService.create(tokenService.requireMemberId(authorization), request));
    }

    @PostMapping("/{orderId}/cancel")
    public AjaxResult cancel(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long orderId, @RequestBody(required = false) MallCancelOrderRequest request)
    {
        String reason = request == null ? null : request.getReason();
        return AjaxResult.success("订单已取消",
                lifecycleService.cancelByMember(tokenService.requireMemberId(authorization), orderId, reason));
    }

    @GetMapping("/{orderId}/logistics")
    public AjaxResult logistics(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long orderId)
    {
        return AjaxResult.success(logisticsService.detailForMember(
                tokenService.requireMemberId(authorization), orderId));
    }

    @PostMapping("/{orderId}/confirm-receipt")
    public AjaxResult confirmReceipt(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long orderId)
    {
        return AjaxResult.success("已确认收货", logisticsService.confirmReceipt(
                tokenService.requireMemberId(authorization), orderId));
    }
}
