package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketCheckinCode;
import com.ruoyi.ticket.mapper.TicketCheckinCodeMapper;
import com.ruoyi.ticket.service.ITicketCheckinCodeService;

/**
 * 票务核销码Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketCheckinCodeServiceImpl implements ITicketCheckinCodeService
{
    @Autowired
    private TicketCheckinCodeMapper ticketCheckinCodeMapper;

    @Override
    public TicketCheckinCode selectTicketCheckinCodeById(Long codeId)
    {
        return ticketCheckinCodeMapper.selectTicketCheckinCodeById(codeId);
    }

    @Override
    public List<TicketCheckinCode> selectTicketCheckinCodeList(TicketCheckinCode ticketCheckinCode)
    {
        return ticketCheckinCodeMapper.selectTicketCheckinCodeList(ticketCheckinCode);
    }

    @Override
    public int insertTicketCheckinCode(TicketCheckinCode ticketCheckinCode)
    {
        return ticketCheckinCodeMapper.insertTicketCheckinCode(ticketCheckinCode);
    }

    @Override
    public int updateTicketCheckinCode(TicketCheckinCode ticketCheckinCode)
    {
        return ticketCheckinCodeMapper.updateTicketCheckinCode(ticketCheckinCode);
    }

    @Override
    public int deleteTicketCheckinCodeByIds(Long[] codeIds)
    {
        return ticketCheckinCodeMapper.deleteTicketCheckinCodeByIds(codeIds);
    }

    @Override
    public int deleteTicketCheckinCodeById(Long codeId)
    {
        return ticketCheckinCodeMapper.deleteTicketCheckinCodeById(codeId);
    }
}
