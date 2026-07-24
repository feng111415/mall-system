package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketUser;
import com.ruoyi.ticket.mapper.TicketUserMapper;
import com.ruoyi.ticket.service.ITicketUserService;

/**
 * 小程序用户Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketUserServiceImpl implements ITicketUserService
{
    @Autowired
    private TicketUserMapper ticketUserMapper;

    @Override
    public TicketUser selectTicketUserById(Long userId)
    {
        return ticketUserMapper.selectTicketUserById(userId);
    }

    @Override
    public List<TicketUser> selectTicketUserList(TicketUser ticketUser)
    {
        return ticketUserMapper.selectTicketUserList(ticketUser);
    }

    @Override
    public int insertTicketUser(TicketUser ticketUser)
    {
        return ticketUserMapper.insertTicketUser(ticketUser);
    }

    @Override
    public int updateTicketUser(TicketUser ticketUser)
    {
        return ticketUserMapper.updateTicketUser(ticketUser);
    }

    @Override
    public int deleteTicketUserByIds(Long[] userIds)
    {
        return ticketUserMapper.deleteTicketUserByIds(userIds);
    }

    @Override
    public int deleteTicketUserById(Long userId)
    {
        return ticketUserMapper.deleteTicketUserById(userId);
    }
}
