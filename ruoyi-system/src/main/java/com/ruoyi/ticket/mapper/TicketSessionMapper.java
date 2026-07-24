package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketSession;

/**
 * 活动场次Mapper接口
 *
 * @author ruoyi
 */
public interface TicketSessionMapper
{
    public TicketSession selectTicketSessionById(Long sessionId);

    public List<TicketSession> selectTicketSessionList(TicketSession ticketSession);

    public int insertTicketSession(TicketSession ticketSession);

    public int updateTicketSession(TicketSession ticketSession);

    public int deleteTicketSessionById(Long sessionId);

    public int deleteTicketSessionByIds(Long[] sessionIds);
}
