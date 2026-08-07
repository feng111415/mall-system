package com.ruoyi.mall.coupon.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.coupon.domain.MallCouponTemplate;
import com.ruoyi.mall.coupon.service.MallCouponAdminService;

@RestController
@RequestMapping("/mall/coupons")
public class MallCouponAdminController extends BaseController
{
    private final MallCouponAdminService service;
    public MallCouponAdminController(MallCouponAdminService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:coupon:list')")
    @GetMapping
    public TableDataInfo list(@RequestParam(required = false) String keyword, @RequestParam(required = false) String status)
    { startPage(); List<MallCouponTemplate> rows = service.list(keyword, status); return getDataTable(rows); }

    @PreAuthorize("@ss.hasPermi('mall:coupon:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }

    @PreAuthorize("@ss.hasAnyPermi('mall:coupon:add,mall:coupon:edit')")
    @Log(title = "商城优惠券", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MallCouponTemplate value) { return success(service.save(value, getUsername())); }

    @PreAuthorize("@ss.hasPermi('mall:coupon:edit')")
    @Log(title = "商城优惠券", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MallCouponTemplate value) { return success(service.save(value, getUsername())); }

    @PreAuthorize("@ss.hasPermi('mall:coupon:publish')")
    @PostMapping("/{id}/status")
    public AjaxResult status(@PathVariable Long id, @RequestParam String status)
    { return success(service.changeStatus(id, status, getUsername())); }

    @PreAuthorize("@ss.hasPermi('mall:coupon:issue')")
    @PostMapping("/{id}/issue")
    public AjaxResult issue(@PathVariable Long id, @RequestParam Long memberId)
    { service.issue(id, memberId, getUsername()); return success(); }
}
