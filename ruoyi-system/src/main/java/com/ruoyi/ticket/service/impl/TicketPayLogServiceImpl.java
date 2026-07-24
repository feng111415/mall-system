package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketPayLog;
import com.ruoyi.ticket.mapper.TicketPayLogMapper;
import com.ruoyi.ticket.service.ITicketPayLogService;

/**
 * 票务支付日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketPayLogServiceImpl implements ITicketPayLogService
{
    @Autowired
    private TicketPayLogMapper ticketPayLogMapper;

    @Override
    public TicketPayLog selectTicketPayLogById(Long payId)
    {
        return ticketPayLogMapper.selectTicketPayLogById(payId);
    }

    @Override
    public List<TicketPayLog> selectTicketPayLogList(TicketPayLog ticketPayLog)
    {
        return ticketPayLogMapper.selectTicketPayLogList(ticketPayLog);
    }

    @Override
    public int insertTicketPayLog(TicketPayLog ticketPayLog)
    {
        return ticketPayLogMapper.insertTicketPayLog(ticketPayLog);
    }

    @Override
    public int updateTicketPayLog(TicketPayLog ticketPayLog)
    {
        return ticketPayLogMapper.updateTicketPayLog(ticketPayLog);
    }

    @Override
    public int deleteTicketPayLogByIds(Long[] payIds)
    {
        return ticketPayLogMapper.deleteTicketPayLogByIds(payIds);
    }

    @Override
    public int deleteTicketPayLogById(Long payId)
    {
        return ticketPayLogMapper.deleteTicketPayLogById(payId);
    }
}
