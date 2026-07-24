package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketUser;

/**
 * 小程序用户Service接口
 *
 * @author ruoyi
 */
public interface ITicketUserService
{
    public TicketUser selectTicketUserById(Long userId);

    public List<TicketUser> selectTicketUserList(TicketUser ticketUser);

    public int insertTicketUser(TicketUser ticketUser);

    public int updateTicketUser(TicketUser ticketUser);

    public int deleteTicketUserByIds(Long[] userIds);

    public int deleteTicketUserById(Long userId);
}
