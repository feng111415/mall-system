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
import com.ruoyi.ticket.domain.TicketUser;
import com.ruoyi.ticket.service.ITicketUserService;

/**
 * 小程序用户Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/user")
public class TicketUserController extends BaseController
{
    @Autowired
    private ITicketUserService ticketUserService;

    @PreAuthorize("@ss.hasPermi('ticket:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketUser ticketUser)
    {
        startPage();
        List<TicketUser> list = ticketUserService.selectTicketUserList(ticketUser);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:user:export')")
    @Log(title = "小程序用户", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketUser ticketUser)
    {
        List<TicketUser> list = ticketUserService.selectTicketUserList(ticketUser);
        ExcelUtil<TicketUser> util = new ExcelUtil<TicketUser>(TicketUser.class);
        util.exportExcel(response, list, "小程序用户数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:user:query')")
    @GetMapping(value = "/{userId}")
    public AjaxResult getInfo(@PathVariable("userId") Long userId)
    {
        return success(ticketUserService.selectTicketUserById(userId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:user:add')")
    @Log(title = "小程序用户", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketUser ticketUser)
    {
        ticketUser.setCreateBy(getUsername());
        return toAjax(ticketUserService.insertTicketUser(ticketUser));
    }

    @PreAuthorize("@ss.hasPermi('ticket:user:edit')")
    @Log(title = "小程序用户", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketUser ticketUser)
    {
        ticketUser.setUpdateBy(getUsername());
        return toAjax(ticketUserService.updateTicketUser(ticketUser));
    }

    @PreAuthorize("@ss.hasPermi('ticket:user:remove')")
    @Log(title = "小程序用户", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        return toAjax(ticketUserService.deleteTicketUserByIds(userIds));
    }
}
