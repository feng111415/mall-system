package com.ruoyi.mall.order.operations.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsQuery;
import com.ruoyi.mall.order.operations.service.MallOrderOperationsService;

@RestController
@RequestMapping("/mall/order-center")
public class MallOrderOperationsAdminController extends BaseController
{
    private final MallOrderOperationsService service;

    public MallOrderOperationsAdminController(MallOrderOperationsService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:order-center:list')")
    @GetMapping
    public TableDataInfo list(@ModelAttribute MallOrderOperationsQuery query)
    {
        startPage();
        List<?> list = service.list(query);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mall:order-center:query')")
    @GetMapping("/{orderId}")
    public AjaxResult detail(@PathVariable Long orderId)
    {
        return success(service.detail(orderId));
    }
}
