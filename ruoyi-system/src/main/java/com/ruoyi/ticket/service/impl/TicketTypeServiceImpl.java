package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketType;
import com.ruoyi.ticket.mapper.TicketTypeMapper;
import com.ruoyi.ticket.service.ITicketTypeService;

/**
 * 票种票档Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketTypeServiceImpl implements ITicketTypeService
{
    @Autowired
    private TicketTypeMapper ticketTypeMapper;

    @Override
    public TicketType selectTicketTypeById(Long typeId)
    {
        return ticketTypeMapper.selectTicketTypeById(typeId);
    }

    @Override
    public List<TicketType> selectTicketTypeList(TicketType ticketType)
    {
        return ticketTypeMapper.selectTicketTypeList(ticketType);
    }

    @Override
    public int insertTicketType(TicketType ticketType)
    {
        return ticketTypeMapper.insertTicketType(ticketType);
    }

    @Override
    public int updateTicketType(TicketType ticketType)
    {
        return ticketTypeMapper.updateTicketType(ticketType);
    }

    @Override
    public int deleteTicketTypeByIds(Long[] typeIds)
    {
        return ticketTypeMapper.deleteTicketTypeByIds(typeIds);
    }

    @Override
    public int deleteTicketTypeById(Long typeId)
    {
        return ticketTypeMapper.deleteTicketTypeById(typeId);
    }
}
