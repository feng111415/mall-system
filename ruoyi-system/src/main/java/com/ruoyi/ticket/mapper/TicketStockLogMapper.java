package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketStockLog;

/**
 * 票务库存流水Mapper接口
 *
 * @author ruoyi
 */
public interface TicketStockLogMapper
{
    public TicketStockLog selectTicketStockLogById(Long logId);

    public List<TicketStockLog> selectTicketStockLogList(TicketStockLog ticketStockLog);

    public int insertTicketStockLog(TicketStockLog ticketStockLog);

    public int updateTicketStockLog(TicketStockLog ticketStockLog);

    public int deleteTicketStockLogById(Long logId);

    public int deleteTicketStockLogByIds(Long[] logIds);
}
