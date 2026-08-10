package com.ruoyi.mall.analytics.web;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.analytics.service.MallBusinessAnalyticsService;

@RestController
@RequestMapping("/mall/business-analytics")
public class MallBusinessAnalyticsAdminController extends BaseController
{
    private final MallBusinessAnalyticsService service;

    public MallBusinessAnalyticsAdminController(MallBusinessAnalyticsService service)
    {
        this.service = service;
    }

    @PreAuthorize("@ss.hasPermi('mall:analytics:list')")
    @GetMapping
    public AjaxResult analytics(@RequestParam(required = false) Integer days)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        List<String> roleKeys = loginUser.getUser().getRoles() == null ? List.of()
                : loginUser.getUser().getRoles().stream().map(SysRole::getRoleKey).collect(Collectors.toList());
        return success(service.analytics(days, loginUser.getUser().isAdmin(), roleKeys));
    }
}
