package com.ruoyi.mall.inventory.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.ruoyi.mall.inventory.domain.MallStock;
import com.ruoyi.mall.inventory.domain.MallStockLog;
import com.ruoyi.mall.inventory.domain.dto.MallStockAdjustRequest;
import com.ruoyi.mall.inventory.service.IMallInventoryService;

@RestController
@RequestMapping("/mall/inventory")
public class MallInventoryAdminController extends BaseController
{
    private final IMallInventoryService service;

    public MallInventoryAdminController(IMallInventoryService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:inventory:list')")
    @GetMapping("/list")
    public TableDataInfo list(MallStock query, @RequestParam(defaultValue = "false") boolean lowStockOnly)
    {
        startPage();
        List<MallStock> list = service.selectStocks(query, lowStockOnly);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:inventory:query')")
    @GetMapping("/{skuId}")
    public AjaxResult detail(@PathVariable Long skuId) { return success(service.selectStock(skuId)); }

    @PreAuthorize("@ss.hasPermi('mall:inventory:query')")
    @GetMapping("/{skuId}/logs")
    public AjaxResult logs(@PathVariable Long skuId, @RequestParam(required = false) String operationType)
    {
        return success(service.selectLogs(skuId, operationType));
    }

    @PreAuthorize("@ss.hasPermi('mall:inventory:adjust')")
    @Log(title = "商城库存调整", businessType = BusinessType.UPDATE)
    @PutMapping("/{skuId}/adjust")
    public AjaxResult adjust(@PathVariable Long skuId, @RequestBody MallStockAdjustRequest request)
    {
        service.adjust(skuId, request, getUsername());
        return success();
    }
}
