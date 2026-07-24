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
import com.ruoyi.ticket.domain.TicketNotifySubscribe;
import com.ruoyi.ticket.service.ITicketNotifySubscribeService;

/**
 * 票务订阅通知Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/notify-subscribe")
public class TicketNotifySubscribeController extends BaseController
{
    @Autowired
    private ITicketNotifySubscribeService ticketNotifySubscribeService;

    @PreAuthorize("@ss.hasPermi('ticket:notify-subscribe:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketNotifySubscribe ticketNotifySubscribe)
    {
        startPage();
        List<TicketNotifySubscribe> list = ticketNotifySubscribeService.selectTicketNotifySubscribeList(ticketNotifySubscribe);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:notify-subscribe:export')")
    @Log(title = "票务订阅通知", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketNotifySubscribe ticketNotifySubscribe)
    {
        List<TicketNotifySubscribe> list = ticketNotifySubscribeService.selectTicketNotifySubscribeList(ticketNotifySubscribe);
        ExcelUtil<TicketNotifySubscribe> util = new ExcelUtil<TicketNotifySubscribe>(TicketNotifySubscribe.class);
        util.exportExcel(response, list, "票务订阅通知数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:notify-subscribe:query')")
    @GetMapping(value = "/{subscribeId}")
    public AjaxResult getInfo(@PathVariable("subscribeId") Long subscribeId)
    {
        return success(ticketNotifySubscribeService.selectTicketNotifySubscribeById(subscribeId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:notify-subscribe:add')")
    @Log(title = "票务订阅通知", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketNotifySubscribe ticketNotifySubscribe)
    {
        ticketNotifySubscribe.setCreateBy(getUsername());
        return toAjax(ticketNotifySubscribeService.insertTicketNotifySubscribe(ticketNotifySubscribe));
    }

    @PreAuthorize("@ss.hasPermi('ticket:notify-subscribe:edit')")
    @Log(title = "票务订阅通知", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketNotifySubscribe ticketNotifySubscribe)
    {
        ticketNotifySubscribe.setUpdateBy(getUsername());
        return toAjax(ticketNotifySubscribeService.updateTicketNotifySubscribe(ticketNotifySubscribe));
    }

    @PreAuthorize("@ss.hasPermi('ticket:notify-subscribe:remove')")
    @Log(title = "票务订阅通知", businessType = BusinessType.DELETE)
    @DeleteMapping("/{subscribeIds}")
    public AjaxResult remove(@PathVariable Long[] subscribeIds)
    {
        return toAjax(ticketNotifySubscribeService.deleteTicketNotifySubscribeByIds(subscribeIds));
    }
}
