package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketStockLog;

/**
 * 票务库存流水Service接口
 *
 * @author ruoyi
 */
public interface ITicketStockLogService
{
    public TicketStockLog selectTicketStockLogById(Long logId);

    public List<TicketStockLog> selectTicketStockLogList(TicketStockLog ticketStockLog);

    public int insertTicketStockLog(TicketStockLog ticketStockLog);

    public int updateTicketStockLog(TicketStockLog ticketStockLog);

    public int deleteTicketStockLogByIds(Long[] logIds);

    public int deleteTicketStockLogById(Long logId);
}
