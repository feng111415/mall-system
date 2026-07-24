package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketUser;

/**
 * 小程序用户Mapper接口
 *
 * @author ruoyi
 */
public interface TicketUserMapper
{
    public TicketUser selectTicketUserById(Long userId);

    public List<TicketUser> selectTicketUserList(TicketUser ticketUser);

    public int insertTicketUser(TicketUser ticketUser);

    public int updateTicketUser(TicketUser ticketUser);

    public int deleteTicketUserById(Long userId);

    public int deleteTicketUserByIds(Long[] userIds);
}
