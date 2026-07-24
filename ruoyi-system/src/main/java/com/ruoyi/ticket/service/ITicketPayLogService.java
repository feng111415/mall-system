package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketPayLog;

/**
 * 票务支付日志Service接口
 *
 * @author ruoyi
 */
public interface ITicketPayLogService
{
    public TicketPayLog selectTicketPayLogById(Long payId);

    public List<TicketPayLog> selectTicketPayLogList(TicketPayLog ticketPayLog);

    public int insertTicketPayLog(TicketPayLog ticketPayLog);

    public int updateTicketPayLog(TicketPayLog ticketPayLog);

    public int deleteTicketPayLogByIds(Long[] payIds);

    public int deleteTicketPayLogById(Long payId);
}
