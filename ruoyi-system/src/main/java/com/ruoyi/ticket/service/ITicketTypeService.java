package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketType;

/**
 * 票种票档Service接口
 *
 * @author ruoyi
 */
public interface ITicketTypeService
{
    public TicketType selectTicketTypeById(Long typeId);

    public List<TicketType> selectTicketTypeList(TicketType ticketType);

    public int insertTicketType(TicketType ticketType);

    public int updateTicketType(TicketType ticketType);

    public int deleteTicketTypeByIds(Long[] typeIds);

    public int deleteTicketTypeById(Long typeId);
}
