package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketOrderItem;
import com.ruoyi.ticket.mapper.TicketOrderItemMapper;
import com.ruoyi.ticket.service.ITicketOrderItemService;

/**
 * 票务订单明细Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketOrderItemServiceImpl implements ITicketOrderItemService
{
    @Autowired
    private TicketOrderItemMapper ticketOrderItemMapper;

    @Override
    public TicketOrderItem selectTicketOrderItemById(Long itemId)
    {
        return ticketOrderItemMapper.selectTicketOrderItemById(itemId);
    }

    @Override
    public List<TicketOrderItem> selectTicketOrderItemList(TicketOrderItem ticketOrderItem)
    {
        return ticketOrderItemMapper.selectTicketOrderItemList(ticketOrderItem);
    }

    @Override
    public int insertTicketOrderItem(TicketOrderItem ticketOrderItem)
    {
        return ticketOrderItemMapper.insertTicketOrderItem(ticketOrderItem);
    }

    @Override
    public int updateTicketOrderItem(TicketOrderItem ticketOrderItem)
    {
        return ticketOrderItemMapper.updateTicketOrderItem(ticketOrderItem);
    }

    @Override
    public int deleteTicketOrderItemByIds(Long[] itemIds)
    {
        return ticketOrderItemMapper.deleteTicketOrderItemByIds(itemIds);
    }

    @Override
    public int deleteTicketOrderItemById(Long itemId)
    {
        return ticketOrderItemMapper.deleteTicketOrderItemById(itemId);
    }
}
