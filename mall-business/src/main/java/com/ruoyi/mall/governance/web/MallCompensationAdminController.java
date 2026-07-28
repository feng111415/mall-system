package com.ruoyi.mall.governance.web;

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
import com.ruoyi.mall.governance.domain.dto.MallCompensationTaskRequest;
import com.ruoyi.mall.governance.service.MallCompensationService;

@RestController
@RequestMapping("/mall/compensation/tasks")
public class MallCompensationAdminController extends BaseController
{
    private final MallCompensationService compensationService;

    public MallCompensationAdminController(MallCompensationService compensationService)
    {
        this.compensationService = compensationService;
    }

    @PreAuthorize("@ss.hasPermi('mall:compensation:list')")
    @GetMapping
    public AjaxResult list(@RequestParam(required = false) String status,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset)
    {
        return success(compensationService.list(status, taskType, limit, offset));
    }

    @PreAuthorize("@ss.hasPermi('mall:compensation:retry')")
    @Log(title = "创建商城补偿任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult create(@Valid @RequestBody MallCompensationTaskRequest request)
    {
        return success(compensationService.create(request.getTaskType(), request.getBusinessKey(),
                request.getOrderNo(), request.getPayload()));
    }

    @PreAuthorize("@ss.hasPermi('mall:compensation:retry')")
    @Log(title = "重试商城补偿任务", businessType = BusinessType.UPDATE)
    @PostMapping("/{taskId}/retry")
    public AjaxResult retry(@PathVariable Long taskId)
    {
        return success(compensationService.retry(taskId));
    }

    @PreAuthorize("@ss.hasPermi('mall:compensation:run')")
    @Log(title = "执行商城补偿任务", businessType = BusinessType.UPDATE)
    @PostMapping("/run")
    public AjaxResult run()
    {
        return AjaxResult.success("补偿任务执行完成", compensationService.runDueTasks());
    }
}
