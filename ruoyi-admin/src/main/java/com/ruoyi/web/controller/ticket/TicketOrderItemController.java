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
import com.ruoyi.ticket.domain.TicketOrderItem;
import com.ruoyi.ticket.service.ITicketOrderItemService;

/**
 * 票务订单明细Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ticket/order-item")
public class TicketOrderItemController extends BaseController
{
    @Autowired
    private ITicketOrderItemService ticketOrderItemService;

    @PreAuthorize("@ss.hasPermi('ticket:order-item:list')")
    @GetMapping("/list")
    public TableDataInfo list(TicketOrderItem ticketOrderItem)
    {
        startPage();
        List<TicketOrderItem> list = ticketOrderItemService.selectTicketOrderItemList(ticketOrderItem);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('ticket:order-item:export')")
    @Log(title = "票务订单明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, TicketOrderItem ticketOrderItem)
    {
        List<TicketOrderItem> list = ticketOrderItemService.selectTicketOrderItemList(ticketOrderItem);
        ExcelUtil<TicketOrderItem> util = new ExcelUtil<TicketOrderItem>(TicketOrderItem.class);
        util.exportExcel(response, list, "票务订单明细数据");
    }

    @PreAuthorize("@ss.hasPermi('ticket:order-item:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        return success(ticketOrderItemService.selectTicketOrderItemById(itemId));
    }

    @PreAuthorize("@ss.hasPermi('ticket:order-item:add')")
    @Log(title = "票务订单明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody TicketOrderItem ticketOrderItem)
    {
        ticketOrderItem.setCreateBy(getUsername());
        return toAjax(ticketOrderItemService.insertTicketOrderItem(ticketOrderItem));
    }

    @PreAuthorize("@ss.hasPermi('ticket:order-item:edit')")
    @Log(title = "票务订单明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody TicketOrderItem ticketOrderItem)
    {
        ticketOrderItem.setUpdateBy(getUsername());
        return toAjax(ticketOrderItemService.updateTicketOrderItem(ticketOrderItem));
    }

    @PreAuthorize("@ss.hasPermi('ticket:order-item:remove')")
    @Log(title = "票务订单明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(ticketOrderItemService.deleteTicketOrderItemByIds(itemIds));
    }
}
