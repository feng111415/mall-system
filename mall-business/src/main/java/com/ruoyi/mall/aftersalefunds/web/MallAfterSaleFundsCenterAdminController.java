package com.ruoyi.mall.aftersalefunds.web;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterQuery;
import com.ruoyi.mall.aftersalefunds.service.MallAfterSaleFundsCenterService;

@RestController
@RequestMapping("/mall/after-sale-funds")
public class MallAfterSaleFundsCenterAdminController extends BaseController
{
    private final MallAfterSaleFundsCenterService service;

    public MallAfterSaleFundsCenterAdminController(MallAfterSaleFundsCenterService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:after-sale-funds:center:query')")
    @GetMapping("/center")
    public AjaxResult center(@RequestParam(required = false) String tab,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String businessNo,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime to,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize)
    {
        MallAfterSaleFundsCenterQuery query = new MallAfterSaleFundsCenterQuery();
        query.setTab(tab); query.setStatus(status); query.setBusinessNo(businessNo); query.setOrderNo(orderNo);
        query.setFrom(from); query.setTo(to); query.setPageNum(pageNum); query.setPageSize(pageSize);
        return success(service.center(query, SecurityUtils.getLoginUser().getPermissions()));
    }
}
