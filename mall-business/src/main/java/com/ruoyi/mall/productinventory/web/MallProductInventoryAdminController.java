package com.ruoyi.mall.productinventory.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryQuery;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySummary;
import com.ruoyi.mall.productinventory.service.MallProductInventoryService;

@RestController
@RequestMapping("/mall/product-inventory")
public class MallProductInventoryAdminController extends BaseController
{
    private final MallProductInventoryService service;

    public MallProductInventoryAdminController(MallProductInventoryService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:product-inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallProductInventoryQuery query)
    {
        startPage();
        List<MallProductInventorySummary> rows = service.list(query);
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('mall:product-inventory:query')")
    @GetMapping("/{spuId}")
    public AjaxResult detail(@PathVariable Long spuId) { return success(service.detail(spuId)); }
}
