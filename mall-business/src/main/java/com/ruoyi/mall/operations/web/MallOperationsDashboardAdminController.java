package com.ruoyi.mall.operations.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.operations.service.MallOperationsDashboardService;

@RestController
@RequestMapping("/mall/operations")
public class MallOperationsDashboardAdminController extends BaseController
{
    private final MallOperationsDashboardService service;

    public MallOperationsDashboardAdminController(MallOperationsDashboardService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:operations:roles')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(service.dashboard(SecurityUtils.getLoginUser().getPermissions()));
    }
}
