package com.ruoyi.mall.logistics.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.logistics.domain.dto.MallShipOrderRequest;
import com.ruoyi.mall.logistics.domain.dto.MallLogisticsNodeRequest;
import com.ruoyi.mall.logistics.service.MallLogisticsService;

@RestController
@RequestMapping("/mall/logistics")
public class MallLogisticsAdminController extends BaseController
{
    private final MallLogisticsService logisticsService;

    public MallLogisticsAdminController(MallLogisticsService logisticsService)
    {
        this.logisticsService = logisticsService;
    }

    @PreAuthorize("@ss.hasPermi('mall:logistics:ship')")
    @Log(title = "商城订单发货", businessType = BusinessType.UPDATE)
    @PostMapping("/orders/{orderId}/ship")
    public AjaxResult ship(@PathVariable Long orderId,
            @Valid @RequestBody(required = false) MallShipOrderRequest request)
    {
        if (request == null) return error("发货信息不能为空");
        return success(logisticsService.shipByAdmin(orderId, request.getCompanyCode(), request.getCompanyName(),
                request.getTrackingNo(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:logistics:query')")
    @GetMapping("/shipments/{shipmentId}")
    public AjaxResult detail(@PathVariable Long shipmentId)
    {
        return success(logisticsService.detailForAdmin(shipmentId));
    }

    @PreAuthorize("@ss.hasPermi('mall:logistics:list')")
    @GetMapping("/orders")
    public AjaxResult orders(@RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status, @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer offset)
    {
        return success(logisticsService.listForAdmin(orderNo, status, limit, offset));
    }

    @PreAuthorize("@ss.hasPermi('mall:logistics:node')")
    @PostMapping("/shipments/{shipmentId}/nodes")
    public AjaxResult appendNode(@PathVariable Long shipmentId, @Valid @RequestBody MallLogisticsNodeRequest request)
    {
        return success(logisticsService.appendNode(shipmentId, request, getUsername()));
    }
}
