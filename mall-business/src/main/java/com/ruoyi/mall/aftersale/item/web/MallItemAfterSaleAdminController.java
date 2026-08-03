package com.ruoyi.mall.aftersale.item.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.aftersale.item.service.MallItemAfterSaleService;
import com.ruoyi.mall.aftersale.domain.dto.MallRefundRequest;

@RestController
@RequestMapping("/mall/after-sale/item-orders")
public class MallItemAfterSaleAdminController extends BaseController
{
    private final MallItemAfterSaleService service;
    public MallItemAfterSaleAdminController(MallItemAfterSaleService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:after-sale:list')")
    @GetMapping
    public AjaxResult list(@RequestParam(required = false) String status) { return success(service.adminList(status)); }

    @PreAuthorize("@ss.hasPermi('mall:after-sale:query')")
    @GetMapping("/{afterSaleId}")
    public AjaxResult detail(@PathVariable Long afterSaleId) { return success(service.adminDetail(afterSaleId)); }

    @PreAuthorize("@ss.hasPermi('mall:after-sale:audit')")
    @Log(title = "商城订单项售后审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{afterSaleId}/approve")
    public AjaxResult approve(@PathVariable Long afterSaleId) { return success(service.approveReview(afterSaleId)); }

    @PreAuthorize("@ss.hasPermi('mall:after-sale:refund')")
    @Log(title = "商城订单项售后退款", businessType = BusinessType.UPDATE)
    @PostMapping("/{afterSaleId}/refund")
    public AjaxResult refund(@PathVariable Long afterSaleId) { return success(service.approve(afterSaleId, getUsername())); }

    @PreAuthorize("@ss.hasPermi('mall:after-sale:audit')")
    @Log(title = "商城订单项售后驳回", businessType = BusinessType.UPDATE)
    @PostMapping("/{afterSaleId}/reject")
    public AjaxResult reject(@PathVariable Long afterSaleId, @RequestBody(required = false) MallRefundRequest request)
    { return success(service.reject(afterSaleId, getUsername(), request == null ? null : request.getReason())); }
}
