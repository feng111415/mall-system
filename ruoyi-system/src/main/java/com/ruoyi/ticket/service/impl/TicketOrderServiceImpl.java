package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketOrder;
import com.ruoyi.ticket.mapper.TicketOrderMapper;
import com.ruoyi.ticket.service.ITicketOrderService;

/**
 * 票务订单Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketOrderServiceImpl implements ITicketOrderService
{
    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Override
    public TicketOrder selectTicketOrderById(Long orderId)
    {
        return ticketOrderMapper.selectTicketOrderById(orderId);
    }

    @Override
    public List<TicketOrder> selectTicketOrderList(TicketOrder ticketOrder)
    {
        return ticketOrderMapper.selectTicketOrderList(ticketOrder);
    }

    @Override
    public int insertTicketOrder(TicketOrder ticketOrder)
    {
        return ticketOrderMapper.insertTicketOrder(ticketOrder);
    }

    @Override
    public int updateTicketOrder(TicketOrder ticketOrder)
    {
        return ticketOrderMapper.updateTicketOrder(ticketOrder);
    }

    @Override
    public int deleteTicketOrderByIds(Long[] orderIds)
    {
        return ticketOrderMapper.deleteTicketOrderByIds(orderIds);
    }

    @Override
    public int deleteTicketOrderById(Long orderId)
    {
        return ticketOrderMapper.deleteTicketOrderById(orderId);
    }
}
