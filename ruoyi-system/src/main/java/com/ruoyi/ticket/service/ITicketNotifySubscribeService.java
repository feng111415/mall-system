package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketNotifySubscribe;

/**
 * 票务订阅通知Service接口
 *
 * @author ruoyi
 */
public interface ITicketNotifySubscribeService
{
    public TicketNotifySubscribe selectTicketNotifySubscribeById(Long subscribeId);

    public List<TicketNotifySubscribe> selectTicketNotifySubscribeList(TicketNotifySubscribe ticketNotifySubscribe);

    public int insertTicketNotifySubscribe(TicketNotifySubscribe ticketNotifySubscribe);

    public int updateTicketNotifySubscribe(TicketNotifySubscribe ticketNotifySubscribe);

    public int deleteTicketNotifySubscribeByIds(Long[] subscribeIds);

    public int deleteTicketNotifySubscribeById(Long subscribeId);
}
