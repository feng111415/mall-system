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
import com.ruoyi.ticket.domain.TicketType;
import com.ruoyi.ticket.service.ITicketTypeService;

/**
 * 票种票档Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/type")
public class TicketTypeController extends BaseController
{
    @Autowired
    private ITicketTypeService ticketTypeService;

    @PreAuthorize("@ss.hasPermi('ticket:type:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketType ticketType)
    {
        startPage();
        List<TicketType> list = ticketTypeService.selectTicketTypeList(ticketType);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:type:export')")
    @Log(title = "票种票档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketType ticketType)
    {
        List<TicketType> list = ticketTypeService.selectTicketTypeList(ticketType);
        ExcelUtil<TicketType> util = new ExcelUtil<TicketType>(TicketType.class);
        util.exportExcel(response, list, "票种票档数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:type:query')")
    @GetMapping(value = "/{typeId}")
    public AjaxResult getInfo(@PathVariable("typeId") Long typeId)
    {
        return success(ticketTypeService.selectTicketTypeById(typeId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:type:add')")
    @Log(title = "票种票档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketType ticketType)
    {
        ticketType.setCreateBy(getUsername());
        return toAjax(ticketTypeService.insertTicketType(ticketType));
    }

    @PreAuthorize("@ss.hasPermi('ticket:type:edit')")
    @Log(title = "票种票档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketType ticketType)
    {
        ticketType.setUpdateBy(getUsername());
        return toAjax(ticketTypeService.updateTicketType(ticketType));
    }

    @PreAuthorize("@ss.hasPermi('ticket:type:remove')")
    @Log(title = "票种票档", businessType = BusinessType.DELETE)
    @DeleteMapping("/{typeIds}")
    public AjaxResult remove(@PathVariable Long[] typeIds)
    {
        return toAjax(ticketTypeService.deleteTicketTypeByIds(typeIds));
    }
}
