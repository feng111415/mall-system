package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketPassenger;
import com.ruoyi.ticket.mapper.TicketPassengerMapper;
import com.ruoyi.ticket.service.ITicketPassengerService;

/**
 * 实名购票人Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketPassengerServiceImpl implements ITicketPassengerService
{
    @Autowired
    private TicketPassengerMapper ticketPassengerMapper;

    @Override
    public TicketPassenger selectTicketPassengerById(Long passengerId)
    {
        return ticketPassengerMapper.selectTicketPassengerById(passengerId);
    }

    @Override
    public List<TicketPassenger> selectTicketPassengerList(TicketPassenger ticketPassenger)
    {
        return ticketPassengerMapper.selectTicketPassengerList(ticketPassenger);
    }

    @Override
    public int insertTicketPassenger(TicketPassenger ticketPassenger)
    {
        return ticketPassengerMapper.insertTicketPassenger(ticketPassenger);
    }

    @Override
    public int updateTicketPassenger(TicketPassenger ticketPassenger)
    {
        return ticketPassengerMapper.updateTicketPassenger(ticketPassenger);
    }

    @Override
    public int deleteTicketPassengerByIds(Long[] passengerIds)
    {
        return ticketPassengerMapper.deleteTicketPassengerByIds(passengerIds);
    }

    @Override
    public int deleteTicketPassengerById(Long passengerId)
    {
        return ticketPassengerMapper.deleteTicketPassengerById(passengerId);
    }
}
