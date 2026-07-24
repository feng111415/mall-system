package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketOrderItem;

/**
 * 票务订单明细Service接口
 *
 * @author ruoyi
 */
public interface ITicketOrderItemService
{
    public TicketOrderItem selectTicketOrderItemById(Long itemId);

    public List<TicketOrderItem> selectTicketOrderItemList(TicketOrderItem ticketOrderItem);

    public int insertTicketOrderItem(TicketOrderItem ticketOrderItem);

    public int updateTicketOrderItem(TicketOrderItem ticketOrderItem);

    public int deleteTicketOrderItemByIds(Long[] itemIds);

    public int deleteTicketOrderItemById(Long itemId);
}
