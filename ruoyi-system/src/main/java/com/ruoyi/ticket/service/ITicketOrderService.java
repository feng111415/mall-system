package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketOrder;

/**
 * 票务订单Service接口
 *
 * @author ruoyi
 */
public interface ITicketOrderService
{
    public TicketOrder selectTicketOrderById(Long orderId);

    public List<TicketOrder> selectTicketOrderList(TicketOrder ticketOrder);

    public int insertTicketOrder(TicketOrder ticketOrder);

    public int updateTicketOrder(TicketOrder ticketOrder);

    public int deleteTicketOrderByIds(Long[] orderIds);

    public int deleteTicketOrderById(Long orderId);
}
