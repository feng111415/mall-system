package com.ruoyi.mall.reconciliation.web;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.reconciliation.domain.dto.MallReconciliationCheckRequest;
import com.ruoyi.mall.reconciliation.domain.dto.MallReconciliationHandleRequest;
import com.ruoyi.mall.reconciliation.service.MallReconciliationService;
import com.ruoyi.mall.reconciliation.domain.MallReconciliationAlert;

@RestController
@RequestMapping("/mall/reconciliation/diffs")
public class MallReconciliationAdminController extends BaseController
{
    private final MallReconciliationService service;

    public MallReconciliationAdminController(MallReconciliationService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:reconciliation:list')")
    @GetMapping
    public AjaxResult list(@RequestParam(required = false) String diffType,
            @RequestParam(required = false) String status, @RequestParam(required = false) String businessNo,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset)
    {
        return success(service.list(diffType, status, businessNo, limit, offset));
    }

    @PreAuthorize("@ss.hasPermi('mall:reconciliation:check')")
    @Log(title = "商城支付退款对账", businessType = BusinessType.INSERT)
    @PostMapping("/check")
    public AjaxResult check(@Valid @RequestBody MallReconciliationCheckRequest request)
    {
        return success(service.check(request.getDiffType(), request.getBusinessNo(), request.getExternalStatus(),
                request.getExternalAmount(), request.getExternalProviderNo(), request.getExternalMessage()));
    }

    @PreAuthorize("@ss.hasPermi('mall:reconciliation:handle')")
    @Log(title = "忽略商城对账差异", businessType = BusinessType.UPDATE)
    @PostMapping("/{diffId}/ignore")
    public AjaxResult ignore(@PathVariable Long diffId,
            @Valid @RequestBody(required = false) MallReconciliationHandleRequest request)
    {
        return success(service.ignore(diffId, request == null ? null : request.getRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:reconciliation:handle')")
    @Log(title = "创建对账补偿任务", businessType = BusinessType.UPDATE)
    @PostMapping("/{diffId}/create-compensation")
    public AjaxResult createCompensation(@PathVariable Long diffId,
            @Valid @RequestBody(required = false) MallReconciliationHandleRequest request)
    {
        return success(service.createCompensation(diffId, request == null ? null : request.getRemark(), getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:reconciliation:alert:list')")
    @GetMapping("/alerts")
    public AjaxResult alerts(@RequestParam(required = false) String status,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset)
    { return success(service.alerts(status, limit, offset)); }

    @PreAuthorize("@ss.hasPermi('mall:reconciliation:alert:ack')")
    @Log(title = "确认商城对账告警", businessType = BusinessType.UPDATE)
    @PostMapping("/alerts/{alertId}/ack")
    public AjaxResult acknowledge(@PathVariable Long alertId)
    { return success(service.acknowledgeAlert(alertId, getUsername())); }
}
