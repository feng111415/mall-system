package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketOrderItem;

/**
 * 票务订单明细Mapper接口
 *
 * @author ruoyi
 */
public interface TicketOrderItemMapper
{
    public TicketOrderItem selectTicketOrderItemById(Long itemId);

    public List<TicketOrderItem> selectTicketOrderItemList(TicketOrderItem ticketOrderItem);

    public int insertTicketOrderItem(TicketOrderItem ticketOrderItem);

    public int updateTicketOrderItem(TicketOrderItem ticketOrderItem);

    public int deleteTicketOrderItemById(Long itemId);

    public int deleteTicketOrderItemByIds(Long[] itemIds);
}
