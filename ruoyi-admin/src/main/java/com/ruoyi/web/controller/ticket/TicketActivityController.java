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
import com.ruoyi.ticket.domain.TicketActivity;
import com.ruoyi.ticket.service.ITicketActivityService;

/**
 * 活动Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/activity")
public class TicketActivityController extends BaseController
{
    @Autowired
    private ITicketActivityService ticketActivityService;

    @PreAuthorize("@ss.hasPermi('ticket:activity:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketActivity ticketActivity)
    {
        startPage();
        List<TicketActivity> list = ticketActivityService.selectTicketActivityList(ticketActivity);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:activity:export')")
    @Log(title = "活动", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketActivity ticketActivity)
    {
        List<TicketActivity> list = ticketActivityService.selectTicketActivityList(ticketActivity);
        ExcelUtil<TicketActivity> util = new ExcelUtil<TicketActivity>(TicketActivity.class);
        util.exportExcel(response, list, "活动数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:activity:query')")
    @GetMapping(value = "/{activityId}")
    public AjaxResult getInfo(@PathVariable("activityId") Long activityId)
    {
        return success(ticketActivityService.selectTicketActivityById(activityId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:activity:add')")
    @Log(title = "活动", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketActivity ticketActivity)
    {
        ticketActivity.setCreateBy(getUsername());
        return toAjax(ticketActivityService.insertTicketActivity(ticketActivity));
    }

    @PreAuthorize("@ss.hasPermi('ticket:activity:edit')")
    @Log(title = "活动", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketActivity ticketActivity)
    {
        ticketActivity.setUpdateBy(getUsername());
        return toAjax(ticketActivityService.updateTicketActivity(ticketActivity));
    }

    @PreAuthorize("@ss.hasPermi('ticket:activity:remove')")
    @Log(title = "活动", businessType = BusinessType.DELETE)
    @DeleteMapping("/{activityIds}")
    public AjaxResult remove(@PathVariable Long[] activityIds)
    {
        return toAjax(ticketActivityService.deleteTicketActivityByIds(activityIds));
    }
}
