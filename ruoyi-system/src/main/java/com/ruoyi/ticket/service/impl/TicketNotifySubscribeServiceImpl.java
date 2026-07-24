package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketNotifySubscribe;
import com.ruoyi.ticket.mapper.TicketNotifySubscribeMapper;
import com.ruoyi.ticket.service.ITicketNotifySubscribeService;

/**
 * 票务订阅通知Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketNotifySubscribeServiceImpl implements ITicketNotifySubscribeService
{
    @Autowired
    private TicketNotifySubscribeMapper ticketNotifySubscribeMapper;

    @Override
    public TicketNotifySubscribe selectTicketNotifySubscribeById(Long subscribeId)
    {
        return ticketNotifySubscribeMapper.selectTicketNotifySubscribeById(subscribeId);
    }

    @Override
    public List<TicketNotifySubscribe> selectTicketNotifySubscribeList(TicketNotifySubscribe ticketNotifySubscribe)
    {
        return ticketNotifySubscribeMapper.selectTicketNotifySubscribeList(ticketNotifySubscribe);
    }

    @Override
    public int insertTicketNotifySubscribe(TicketNotifySubscribe ticketNotifySubscribe)
    {
        return ticketNotifySubscribeMapper.insertTicketNotifySubscribe(ticketNotifySubscribe);
    }

    @Override
    public int updateTicketNotifySubscribe(TicketNotifySubscribe ticketNotifySubscribe)
    {
        return ticketNotifySubscribeMapper.updateTicketNotifySubscribe(ticketNotifySubscribe);
    }

    @Override
    public int deleteTicketNotifySubscribeByIds(Long[] subscribeIds)
    {
        return ticketNotifySubscribeMapper.deleteTicketNotifySubscribeByIds(subscribeIds);
    }

    @Override
    public int deleteTicketNotifySubscribeById(Long subscribeId)
    {
        return ticketNotifySubscribeMapper.deleteTicketNotifySubscribeById(subscribeId);
    }
}
