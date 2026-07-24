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
import com.ruoyi.ticket.domain.TicketSession;
import com.ruoyi.ticket.service.ITicketSessionService;

/**
 * 活动场次Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/session")
public class TicketSessionController extends BaseController
{
    @Autowired
    private ITicketSessionService ticketSessionService;

    @PreAuthorize("@ss.hasPermi('ticket:session:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketSession ticketSession)
    {
        startPage();
        List<TicketSession> list = ticketSessionService.selectTicketSessionList(ticketSession);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:session:export')")
    @Log(title = "活动场次", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketSession ticketSession)
    {
        List<TicketSession> list = ticketSessionService.selectTicketSessionList(ticketSession);
        ExcelUtil<TicketSession> util = new ExcelUtil<TicketSession>(TicketSession.class);
        util.exportExcel(response, list, "活动场次数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:session:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId)
    {
        return success(ticketSessionService.selectTicketSessionById(sessionId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:session:add')")
    @Log(title = "活动场次", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketSession ticketSession)
    {
        ticketSession.setCreateBy(getUsername());
        return toAjax(ticketSessionService.insertTicketSession(ticketSession));
    }

    @PreAuthorize("@ss.hasPermi('ticket:session:edit')")
    @Log(title = "活动场次", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketSession ticketSession)
    {
        ticketSession.setUpdateBy(getUsername());
        return toAjax(ticketSessionService.updateTicketSession(ticketSession));
    }

    @PreAuthorize("@ss.hasPermi('ticket:session:remove')")
    @Log(title = "活动场次", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds)
    {
        return toAjax(ticketSessionService.deleteTicketSessionByIds(sessionIds));
    }
}
