package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketStock;
import com.ruoyi.ticket.mapper.TicketStockMapper;
import com.ruoyi.ticket.service.ITicketStockService;

/**
 * 票务库存Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketStockServiceImpl implements ITicketStockService
{
    @Autowired
    private TicketStockMapper ticketStockMapper;

    @Override
    public TicketStock selectTicketStockById(Long stockId)
    {
        return ticketStockMapper.selectTicketStockById(stockId);
    }

    @Override
    public List<TicketStock> selectTicketStockList(TicketStock ticketStock)
    {
        return ticketStockMapper.selectTicketStockList(ticketStock);
    }

    @Override
    public int insertTicketStock(TicketStock ticketStock)
    {
        return ticketStockMapper.insertTicketStock(ticketStock);
    }

    @Override
    public int updateTicketStock(TicketStock ticketStock)
    {
        return ticketStockMapper.updateTicketStock(ticketStock);
    }

    @Override
    public int deleteTicketStockByIds(Long[] stockIds)
    {
        return ticketStockMapper.deleteTicketStockByIds(stockIds);
    }

    @Override
    public int deleteTicketStockById(Long stockId)
    {
        return ticketStockMapper.deleteTicketStockById(stockId);
    }
}
