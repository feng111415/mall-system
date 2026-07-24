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
import com.ruoyi.ticket.domain.TicketPassenger;
import com.ruoyi.ticket.service.ITicketPassengerService;

/**
 * 实名购票人Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/passenger")
public class TicketPassengerController extends BaseController
{
    @Autowired
    private ITicketPassengerService ticketPassengerService;

    @PreAuthorize("@ss.hasPermi('ticket:passenger:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketPassenger ticketPassenger)
    {
        startPage();
        List<TicketPassenger> list = ticketPassengerService.selectTicketPassengerList(ticketPassenger);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:passenger:export')")
    @Log(title = "实名购票人", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketPassenger ticketPassenger)
    {
        List<TicketPassenger> list = ticketPassengerService.selectTicketPassengerList(ticketPassenger);
        ExcelUtil<TicketPassenger> util = new ExcelUtil<TicketPassenger>(TicketPassenger.class);
        util.exportExcel(response, list, "实名购票人数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:passenger:query')")
    @GetMapping(value = "/{passengerId}")
    public AjaxResult getInfo(@PathVariable("passengerId") Long passengerId)
    {
        return success(ticketPassengerService.selectTicketPassengerById(passengerId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:passenger:add')")
    @Log(title = "实名购票人", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketPassenger ticketPassenger)
    {
        ticketPassenger.setCreateBy(getUsername());
        return toAjax(ticketPassengerService.insertTicketPassenger(ticketPassenger));
    }

    @PreAuthorize("@ss.hasPermi('ticket:passenger:edit')")
    @Log(title = "实名购票人", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketPassenger ticketPassenger)
    {
        ticketPassenger.setUpdateBy(getUsername());
        return toAjax(ticketPassengerService.updateTicketPassenger(ticketPassenger));
    }

    @PreAuthorize("@ss.hasPermi('ticket:passenger:remove')")
    @Log(title = "实名购票人", businessType = BusinessType.DELETE)
    @DeleteMapping("/{passengerIds}")
    public AjaxResult remove(@PathVariable Long[] passengerIds)
    {
        return toAjax(ticketPassengerService.deleteTicketPassengerByIds(passengerIds));
    }
}
