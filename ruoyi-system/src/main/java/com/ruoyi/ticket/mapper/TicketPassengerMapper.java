package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketPassenger;

/**
 * 实名购票人Mapper接口
 *
 * @author ruoyi
 */
public interface TicketPassengerMapper
{
    public TicketPassenger selectTicketPassengerById(Long passengerId);

    public List<TicketPassenger> selectTicketPassengerList(TicketPassenger ticketPassenger);

    public int insertTicketPassenger(TicketPassenger ticketPassenger);

    public int updateTicketPassenger(TicketPassenger ticketPassenger);

    public int deleteTicketPassengerById(Long passengerId);

    public int deleteTicketPassengerByIds(Long[] passengerIds);
}
