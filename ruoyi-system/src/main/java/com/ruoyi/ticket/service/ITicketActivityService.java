package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketActivity;

/**
 * 活动Service接口
 *
 * @author ruoyi
 */
public interface ITicketActivityService
{
    public TicketActivity selectTicketActivityById(Long activityId);

    public List<TicketActivity> selectTicketActivityList(TicketActivity ticketActivity);

    public int insertTicketActivity(TicketActivity ticketActivity);

    public int updateTicketActivity(TicketActivity ticketActivity);

    public int deleteTicketActivityByIds(Long[] activityIds);

    public int deleteTicketActivityById(Long activityId);
}
