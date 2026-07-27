package com.ruoyi.mall.aftersale.web;

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
import com.ruoyi.mall.aftersale.domain.dto.MallRefundRequest;
import com.ruoyi.mall.aftersale.service.MallRefundService;

@RestController
@RequestMapping("/mall/after-sale/refunds")
public class MallRefundAdminController extends BaseController
{
    private final MallRefundService refundService;

    public MallRefundAdminController(MallRefundService refundService) { this.refundService = refundService; }

    @PreAuthorize("@ss.hasPermi('mall:refund:audit')")
    @Log(title = "商城退款审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{refundId}/approve")
    public AjaxResult approve(@PathVariable Long refundId)
    {
        return success(refundService.approve(refundId, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:refund:audit')")
    @Log(title = "商城退款驳回", businessType = BusinessType.UPDATE)
    @PostMapping("/{refundId}/reject")
    public AjaxResult reject(@PathVariable Long refundId,
            @Valid @RequestBody(required = false) MallRefundRequest request)
    {
        String reason = request == null ? null : request.getReason();
        return success(refundService.reject(refundId, getUsername(), reason));
    }
}
