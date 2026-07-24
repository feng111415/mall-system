package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketActivity;

/**
 * 活动Mapper接口
 *
 * @author ruoyi
 */
public interface TicketActivityMapper
{
    public TicketActivity selectTicketActivityById(Long activityId);

    public List<TicketActivity> selectTicketActivityList(TicketActivity ticketActivity);

    public int insertTicketActivity(TicketActivity ticketActivity);

    public int updateTicketActivity(TicketActivity ticketActivity);

    public int deleteTicketActivityById(Long activityId);

    public int deleteTicketActivityByIds(Long[] activityIds);
}
