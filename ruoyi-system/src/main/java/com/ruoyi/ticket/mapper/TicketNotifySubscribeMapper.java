package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketNotifySubscribe;

/**
 * 票务订阅通知Mapper接口
 *
 * @author ruoyi
 */
public interface TicketNotifySubscribeMapper
{
    public TicketNotifySubscribe selectTicketNotifySubscribeById(Long subscribeId);

    public List<TicketNotifySubscribe> selectTicketNotifySubscribeList(TicketNotifySubscribe ticketNotifySubscribe);

    public int insertTicketNotifySubscribe(TicketNotifySubscribe ticketNotifySubscribe);

    public int updateTicketNotifySubscribe(TicketNotifySubscribe ticketNotifySubscribe);

    public int deleteTicketNotifySubscribeById(Long subscribeId);

    public int deleteTicketNotifySubscribeByIds(Long[] subscribeIds);
}
