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
import com.ruoyi.ticket.domain.TicketCheckinCode;
import com.ruoyi.ticket.service.ITicketCheckinCodeService;

/**
 * 票务核销码Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/checkin-code")
public class TicketCheckinCodeController extends BaseController
{
    @Autowired
    private ITicketCheckinCodeService ticketCheckinCodeService;

    @PreAuthorize("@ss.hasPermi('ticket:checkin-code:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketCheckinCode ticketCheckinCode)
    {
        startPage();
        List<TicketCheckinCode> list = ticketCheckinCodeService.selectTicketCheckinCodeList(ticketCheckinCode);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:checkin-code:export')")
    @Log(title = "票务核销码", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketCheckinCode ticketCheckinCode)
    {
        List<TicketCheckinCode> list = ticketCheckinCodeService.selectTicketCheckinCodeList(ticketCheckinCode);
        ExcelUtil<TicketCheckinCode> util = new ExcelUtil<TicketCheckinCode>(TicketCheckinCode.class);
        util.exportExcel(response, list, "票务核销码数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:checkin-code:query')")
    @GetMapping(value = "/{codeId}")
    public AjaxResult getInfo(@PathVariable("codeId") Long codeId)
    {
        return success(ticketCheckinCodeService.selectTicketCheckinCodeById(codeId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:checkin-code:add')")
    @Log(title = "票务核销码", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketCheckinCode ticketCheckinCode)
    {
        ticketCheckinCode.setCreateBy(getUsername());
        return toAjax(ticketCheckinCodeService.insertTicketCheckinCode(ticketCheckinCode));
    }

    @PreAuthorize("@ss.hasPermi('ticket:checkin-code:edit')")
    @Log(title = "票务核销码", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketCheckinCode ticketCheckinCode)
    {
        ticketCheckinCode.setUpdateBy(getUsername());
        return toAjax(ticketCheckinCodeService.updateTicketCheckinCode(ticketCheckinCode));
    }

    @PreAuthorize("@ss.hasPermi('ticket:checkin-code:remove')")
    @Log(title = "票务核销码", businessType = BusinessType.DELETE)
    @DeleteMapping("/{codeIds}")
    public AjaxResult remove(@PathVariable Long[] codeIds)
    {
        return toAjax(ticketCheckinCodeService.deleteTicketCheckinCodeByIds(codeIds));
    }
}
