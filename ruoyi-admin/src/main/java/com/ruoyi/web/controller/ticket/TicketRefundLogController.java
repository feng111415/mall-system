package com.ruoyi.web.controller.ticket;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.ticket.domain.TicketRefundLog;
import com.ruoyi.ticket.service.ITicketRefundLogService;

/**
 * 票务退款日志Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/refund-log")
public class TicketRefundLogController extends BaseController
{
    @Autowired
    private ITicketRefundLogService ticketRefundLogService;

    @PreAuthorize("@ss.hasPermi('ticket:refund-log:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketRefundLog ticketRefundLog)
    {
        startPage();
        List<TicketRefundLog> list = ticketRefundLogService.selectTicketRefundLogList(ticketRefundLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:refund-log:export')")
    @Log(title = "票务退款日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketRefundLog ticketRefundLog)
    {
        List<TicketRefundLog> list = ticketRefundLogService.selectTicketRefundLogList(ticketRefundLog);
        ExcelUtil<TicketRefundLog> util = new ExcelUtil<TicketRefundLog>(TicketRefundLog.class);
        util.exportExcel(response, list, "票务退款日志数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:refund-log:query')")
    @GetMapping(value = "/{refundId}")
    public AjaxResult getInfo(@PathVariable("refundId") Long refundId)
    {
        return success(ticketRefundLogService.selectTicketRefundLogById(refundId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:refund-log:add')")
    @Log(title = "票务退款日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketRefundLog ticketRefundLog)
    {
        ticketRefundLog.setCreateBy(getUsername());
        return toAjax(ticketRefundLogService.insertTicketRefundLog(ticketRefundLog));
    }

    @PreAuthorize("@ss.hasPermi('ticket:refund-log:edit')")
    @Log(title = "票务退款日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketRefundLog ticketRefundLog)
    {
        ticketRefundLog.setUpdateBy(getUsername());
        return toAjax(ticketRefundLogService.updateTicketRefundLog(ticketRefundLog));
    }

    @PreAuthorize("@ss.hasPermi('ticket:refund-log:remove')")
    @Log(title = "票务退款日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{refundIds}")
    public AjaxResult remove(@PathVariable Long[] refundIds)
    {
        return toAjax(ticketRefundLogService.deleteTicketRefundLogByIds(refundIds));
    }
}
