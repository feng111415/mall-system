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
import com.ruoyi.ticket.domain.TicketPayLog;
import com.ruoyi.ticket.service.ITicketPayLogService;

/**
 * 票务支付日志Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/pay-log")
public class TicketPayLogController extends BaseController
{
    @Autowired
    private ITicketPayLogService ticketPayLogService;

    @PreAuthorize("@ss.hasPermi('ticket:pay-log:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketPayLog ticketPayLog)
    {
        startPage();
        List<TicketPayLog> list = ticketPayLogService.selectTicketPayLogList(ticketPayLog);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:pay-log:export')")
    @Log(title = "票务支付日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketPayLog ticketPayLog)
    {
        List<TicketPayLog> list = ticketPayLogService.selectTicketPayLogList(ticketPayLog);
        ExcelUtil<TicketPayLog> util = new ExcelUtil<TicketPayLog>(TicketPayLog.class);
        util.exportExcel(response, list, "票务支付日志数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:pay-log:query')")
    @GetMapping(value = "/{payId}")
    public AjaxResult getInfo(@PathVariable("payId") Long payId)
    {
        return success(ticketPayLogService.selectTicketPayLogById(payId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:pay-log:add')")
    @Log(title = "票务支付日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketPayLog ticketPayLog)
    {
        ticketPayLog.setCreateBy(getUsername());
        return toAjax(ticketPayLogService.insertTicketPayLog(ticketPayLog));
    }

    @PreAuthorize("@ss.hasPermi('ticket:pay-log:edit')")
    @Log(title = "票务支付日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketPayLog ticketPayLog)
    {
        ticketPayLog.setUpdateBy(getUsername());
        return toAjax(ticketPayLogService.updateTicketPayLog(ticketPayLog));
    }

    @PreAuthorize("@ss.hasPermi('ticket:pay-log:remove')")
    @Log(title = "票务支付日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{payIds}")
    public AjaxResult remove(@PathVariable Long[] payIds)
    {
        return toAjax(ticketPayLogService.deleteTicketPayLogByIds(payIds));
    }
}
