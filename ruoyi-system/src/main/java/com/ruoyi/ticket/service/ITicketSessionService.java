package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketSession;

/**
 * 活动场次Service接口
 *
 * @author ruoyi
 */
public interface ITicketSessionService
{
    public TicketSession selectTicketSessionById(Long sessionId);

    public List<TicketSession> selectTicketSessionList(TicketSession ticketSession);

    public int insertTicketSession(TicketSession ticketSession);

    public int updateTicketSession(TicketSession ticketSession);

    public int deleteTicketSessionByIds(Long[] sessionIds);

    public int deleteTicketSessionById(Long sessionId);
}
