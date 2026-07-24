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
import com.ruoyi.ticket.domain.TicketStock;
import com.ruoyi.ticket.service.ITicketStockService;

/**
 * 票务库存Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/stock")
public class TicketStockController extends BaseController
{
    @Autowired
    private ITicketStockService ticketStockService;

    @PreAuthorize("@ss.hasPermi('ticket:stock:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketStock ticketStock)
    {
        startPage();
        List<TicketStock> list = ticketStockService.selectTicketStockList(ticketStock);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock:export')")
    @Log(title = "票务库存", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketStock ticketStock)
    {
        List<TicketStock> list = ticketStockService.selectTicketStockList(ticketStock);
        ExcelUtil<TicketStock> util = new ExcelUtil<TicketStock>(TicketStock.class);
        util.exportExcel(response, list, "票务库存数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock:query')")
    @GetMapping(value = "/{stockId}")
    public AjaxResult getInfo(@PathVariable("stockId") Long stockId)
    {
        return success(ticketStockService.selectTicketStockById(stockId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock:add')")
    @Log(title = "票务库存", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketStock ticketStock)
    {
        ticketStock.setCreateBy(getUsername());
        return toAjax(ticketStockService.insertTicketStock(ticketStock));
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock:edit')")
    @Log(title = "票务库存", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketStock ticketStock)
    {
        ticketStock.setUpdateBy(getUsername());
        return toAjax(ticketStockService.updateTicketStock(ticketStock));
    }

    @PreAuthorize("@ss.hasPermi('ticket:stock:remove')")
    @Log(title = "票务库存", businessType = BusinessType.DELETE)
    @DeleteMapping("/{stockIds}")
    public AjaxResult remove(@PathVariable Long[] stockIds)
    {
        return toAjax(ticketStockService.deleteTicketStockByIds(stockIds));
    }
}
