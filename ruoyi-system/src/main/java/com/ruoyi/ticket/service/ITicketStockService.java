package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketStock;

/**
 * 票务库存Service接口
 *
 * @author ruoyi
 */
public interface ITicketStockService
{
    public TicketStock selectTicketStockById(Long stockId);

    public List<TicketStock> selectTicketStockList(TicketStock ticketStock);

    public int insertTicketStock(TicketStock ticketStock);

    public int updateTicketStock(TicketStock ticketStock);

    public int deleteTicketStockByIds(Long[] stockIds);

    public int deleteTicketStockById(Long stockId);
}
