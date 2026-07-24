package com.ruoyi.ticket.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.ticket.domain.TicketActivity;
import com.ruoyi.ticket.mapper.TicketActivityMapper;
import com.ruoyi.ticket.service.ITicketActivityService;

/**
 * 活动Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class TicketActivityServiceImpl implements ITicketActivityService
{
    @Autowired
    private TicketActivityMapper ticketActivityMapper;

    @Override
    public TicketActivity selectTicketActivityById(Long activityId)
    {
        return ticketActivityMapper.selectTicketActivityById(activityId);
    }

    @Override
    public List<TicketActivity> selectTicketActivityList(TicketActivity ticketActivity)
    {
        return ticketActivityMapper.selectTicketActivityList(ticketActivity);
    }

    @Override
    public int insertTicketActivity(TicketActivity ticketActivity)
    {
        return ticketActivityMapper.insertTicketActivity(ticketActivity);
    }

    @Override
    public int updateTicketActivity(TicketActivity ticketActivity)
    {
        return ticketActivityMapper.updateTicketActivity(ticketActivity);
    }

    @Override
    public int deleteTicketActivityByIds(Long[] activityIds)
    {
        return ticketActivityMapper.deleteTicketActivityByIds(activityIds);
    }

    @Override
    public int deleteTicketActivityById(Long activityId)
    {
        return ticketActivityMapper.deleteTicketActivityById(activityId);
    }
}
