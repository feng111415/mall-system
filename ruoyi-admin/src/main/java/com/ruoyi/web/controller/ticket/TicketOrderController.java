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
import com.ruoyi.ticket.domain.TicketOrder;
import com.ruoyi.ticket.service.ITicketOrderService;

/**
 * 票务订单Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/order")
public class TicketOrderController extends BaseController
{
    @Autowired
    private ITicketOrderService ticketOrderService;

    @PreAuthorize("@ss.hasPermi('ticket:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketOrder ticketOrder)
    {
        startPage();
        List<TicketOrder> list = ticketOrderService.selectTicketOrderList(ticketOrder);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:order:export')")
    @Log(title = "票务订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketOrder ticketOrder)
    {
        List<TicketOrder> list = ticketOrderService.selectTicketOrderList(ticketOrder);
        ExcelUtil<TicketOrder> util = new ExcelUtil<TicketOrder>(TicketOrder.class);
        util.exportExcel(response, list, "票务订单数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:order:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return success(ticketOrderService.selectTicketOrderById(orderId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:order:add')")
    @Log(title = "票务订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketOrder ticketOrder)
    {
        ticketOrder.setCreateBy(getUsername());
        return toAjax(ticketOrderService.insertTicketOrder(ticketOrder));
    }

    @PreAuthorize("@ss.hasPermi('ticket:order:edit')")
    @Log(title = "票务订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketOrder ticketOrder)
    {
        ticketOrder.setUpdateBy(getUsername());
        return toAjax(ticketOrderService.updateTicketOrder(ticketOrder));
    }

    @PreAuthorize("@ss.hasPermi('ticket:order:remove')")
    @Log(title = "票务订单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(ticketOrderService.deleteTicketOrderByIds(orderIds));
    }
}
