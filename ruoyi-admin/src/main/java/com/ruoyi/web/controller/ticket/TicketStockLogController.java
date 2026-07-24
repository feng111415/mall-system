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
import com.ruoyi.ticket.domain.TicketStockLog;
import com.ruoyi.ticket.service.ITicketStockLogService;

/**
 * 票务库存流水Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/stock-log")
public class TicketStockLogController extends BaseController
{
    @Autowired
    private ITicketStockLogService ticketStockLogService;

    @PreAuthorize("@ss.hasPermi('ticket:stock-log:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketStockLog ticketStockLog)
    {
        startPage();
        List<TicketStockLog> list = ticketStockLogService.selectTicketStockLogList(ticketStockLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock-log:export')")
    @Log(title = "票务库存流水", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketStockLog ticketStockLog)
    {
        List<TicketStockLog> list = ticketStockLogService.selectTicketStockLogList(ticketStockLog);
        ExcelUtil<TicketStockLog> util = new ExcelUtil<TicketStockLog>(TicketStockLog.class);
        util.exportExcel(response, list, "票务库存流水数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock-log:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(ticketStockLogService.selectTicketStockLogById(logId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock-log:add')")
    @Log(title = "票务库存流水", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketStockLog ticketStockLog)
    {
        ticketStockLog.setCreateBy(getUsername());
        return toAjax(ticketStockLogService.insertTicketStockLog(ticketStockLog));
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock-log:edit')")
    @Log(title = "票务库存流水", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketStockLog ticketStockLog)
    {
        ticketStockLog.setUpdateBy(getUsername());
        return toAjax(ticketStockLogService.updateTicketStockLog(ticketStockLog));
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock-log:remove')")
    @Log(title = "票务库存流水", businessType = BusinessType.DELETE)
    @DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(ticketStockLogService.deleteTicketStockLogByIds(logIds));
    }
}
