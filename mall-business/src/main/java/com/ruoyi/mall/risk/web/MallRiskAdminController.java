package com.ruoyi.mall.risk.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.risk.service.MallRiskService;

@RestController
@RequestMapping("/mall/risk")
public class MallRiskAdminController extends BaseController
{
    private final MallRiskService riskService;

    public MallRiskAdminController(MallRiskService riskService) { this.riskService = riskService; }

    @PreAuthorize("@ss.hasPermi('mall:risk:list')")
    @GetMapping("/records")
    public AjaxResult records(@RequestParam(required = false) String decision,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset)
    {
        return success(riskService.records(decision, limit, offset));
    }
}
