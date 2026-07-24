package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketRefundLog;
import com.ruoyi.ticket.mapper.TicketRefundLogMapper;
import com.ruoyi.ticket.service.ITicketRefundLogService;

/**
 * 票务退款日志Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketRefundLogServiceImpl implements ITicketRefundLogService
{
    @Autowired
    private TicketRefundLogMapper ticketRefundLogMapper;

    @Override
    public TicketRefundLog selectTicketRefundLogById(Long refundId)
    {
        return ticketRefundLogMapper.selectTicketRefundLogById(refundId);
    }

    @Override
    public List<TicketRefundLog> selectTicketRefundLogList(TicketRefundLog ticketRefundLog)
    {
        return ticketRefundLogMapper.selectTicketRefundLogList(ticketRefundLog);
    }

    @Override
    public int insertTicketRefundLog(TicketRefundLog ticketRefundLog)
    {
        return ticketRefundLogMapper.insertTicketRefundLog(ticketRefundLog);
    }

    @Override
    public int updateTicketRefundLog(TicketRefundLog ticketRefundLog)
    {
        return ticketRefundLogMapper.updateTicketRefundLog(ticketRefundLog);
    }

    @Override
    public int deleteTicketRefundLogByIds(Long[] refundIds)
    {
        return ticketRefundLogMapper.deleteTicketRefundLogByIds(refundIds);
    }

    @Override
    public int deleteTicketRefundLogById(Long refundId)
    {
        return ticketRefundLogMapper.deleteTicketRefundLogById(refundId);
    }
}
