package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketPassenger;

/**
 * 实名购票人Service接口
 *
 * @author ruoyi
 */
public interface ITicketPassengerService
{
    public TicketPassenger selectTicketPassengerById(Long passengerId);

    public List<TicketPassenger> selectTicketPassengerList(TicketPassenger ticketPassenger);

    public int insertTicketPassenger(TicketPassenger ticketPassenger);

    public int updateTicketPassenger(TicketPassenger ticketPassenger);

    public int deleteTicketPassengerByIds(Long[] passengerIds);

    public int deleteTicketPassengerById(Long passengerId);
}
