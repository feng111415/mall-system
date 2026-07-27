package com.ruoyi.mall.audit.web;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mall.audit.domain.MallOrderOperationLogQuery;
import com.ruoyi.mall.audit.service.MallAuditService;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

@RestController
@RequestMapping("/mall/audit")
public class MallAuditAdminController extends BaseController
{
    private final MallAuditService auditService;

    public MallAuditAdminController(MallAuditService auditService) { this.auditService = auditService; }

    @PreAuthorize("@ss.hasPermi('mall:audit:list')")
    @GetMapping("/order-operations")
    public TableDataInfo orderOperations(@ModelAttribute MallOrderOperationLogQuery query)
    {
        startPage();
        List<MallOrderOperationLog> list = auditService.orderOperationLogs(query);
        return getDataTable(list);
    }
}
