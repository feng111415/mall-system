package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketSession;
import com.ruoyi.ticket.mapper.TicketSessionMapper;
import com.ruoyi.ticket.service.ITicketSessionService;

/**
 * 活动场次Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketSessionServiceImpl implements ITicketSessionService
{
    @Autowired
    private TicketSessionMapper ticketSessionMapper;

    @Override
    public TicketSession selectTicketSessionById(Long sessionId)
    {
        return ticketSessionMapper.selectTicketSessionById(sessionId);
    }

    @Override
    public List<TicketSession> selectTicketSessionList(TicketSession ticketSession)
    {
        return ticketSessionMapper.selectTicketSessionList(ticketSession);
    }

    @Override
    public int insertTicketSession(TicketSession ticketSession)
    {
        return ticketSessionMapper.insertTicketSession(ticketSession);
    }

    @Override
    public int updateTicketSession(TicketSession ticketSession)
    {
        return ticketSessionMapper.updateTicketSession(ticketSession);
    }

    @Override
    public int deleteTicketSessionByIds(Long[] sessionIds)
    {
        return ticketSessionMapper.deleteTicketSessionByIds(sessionIds);
    }

    @Override
    public int deleteTicketSessionById(Long sessionId)
    {
        return ticketSessionMapper.deleteTicketSessionById(sessionId);
    }
}
