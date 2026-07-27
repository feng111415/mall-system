package com.ruoyi.mall.logistics.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.logistics.domain.dto.MallShipOrderRequest;
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
        String companyCode = request == null ? null : request.getCompanyCode();
        return success(logisticsService.shipByAdmin(orderId, companyCode, getUsername()));
    }
}
