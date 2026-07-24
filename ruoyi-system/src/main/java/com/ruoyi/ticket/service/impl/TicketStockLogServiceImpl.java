package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketStockLog;
import com.ruoyi.ticket.mapper.TicketStockLogMapper;
import com.ruoyi.ticket.service.ITicketStockLogService;

/**
 * 票务库存流水Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketStockLogServiceImpl implements ITicketStockLogService
{
    @Autowired
    private TicketStockLogMapper ticketStockLogMapper;

    @Override
    public TicketStockLog selectTicketStockLogById(Long logId)
    {
        return ticketStockLogMapper.selectTicketStockLogById(logId);
    }

    @Override
    public List<TicketStockLog> selectTicketStockLogList(TicketStockLog ticketStockLog)
    {
        return ticketStockLogMapper.selectTicketStockLogList(ticketStockLog);
    }

    @Override
    public int insertTicketStockLog(TicketStockLog ticketStockLog)
    {
        return ticketStockLogMapper.insertTicketStockLog(ticketStockLog);
    }

    @Override
    public int updateTicketStockLog(TicketStockLog ticketStockLog)
    {
        return ticketStockLogMapper.updateTicketStockLog(ticketStockLog);
    }

    @Override
    public int deleteTicketStockLogByIds(Long[] logIds)
    {
        return ticketStockLogMapper.deleteTicketStockLogByIds(logIds);
    }

    @Override
    public int deleteTicketStockLogById(Long logId)
    {
        return ticketStockLogMapper.deleteTicketStockLogById(logId);
    }
}
