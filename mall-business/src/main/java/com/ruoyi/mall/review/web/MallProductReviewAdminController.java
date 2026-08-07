package com.ruoyi.mall.review.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.review.domain.MallProductReview;
import com.ruoyi.mall.review.domain.MallProductReviewQuery;
import com.ruoyi.mall.review.domain.dto.MallProductReviewAuditRequest;
import com.ruoyi.mall.review.service.MallProductReviewService;

@RestController
@RequestMapping("/mall/reviews")
public class MallProductReviewAdminController extends BaseController
{
    private final MallProductReviewService service;
    public MallProductReviewAdminController(MallProductReviewService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:review:list')")
    @GetMapping
    public TableDataInfo list(MallProductReviewQuery query)
    {
        startPage(); List<MallProductReview> rows = service.adminList(query); return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('mall:review:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) { return success(service.detail(id)); }

    @PreAuthorize("@ss.hasPermi('mall:review:audit')")
    @Log(title = "商品评价审核", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/audit")
    public AjaxResult audit(@PathVariable Long id, @RequestBody MallProductReviewAuditRequest request)
    { return success(service.audit(id, request, getUsername())); }
}
