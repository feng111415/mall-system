package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketType;

/**
 * 票种票档Mapper接口
 *
 * @author ruoyi
 */
public interface TicketTypeMapper
{
    public TicketType selectTicketTypeById(Long typeId);

    public List<TicketType> selectTicketTypeList(TicketType ticketType);

    public int insertTicketType(TicketType ticketType);

    public int updateTicketType(TicketType ticketType);

    public int deleteTicketTypeById(Long typeId);

    public int deleteTicketTypeByIds(Long[] typeIds);
}
