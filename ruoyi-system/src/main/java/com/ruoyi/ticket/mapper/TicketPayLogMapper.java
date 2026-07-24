package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketPayLog;

/**
 * 票务支付日志Mapper接口
 *
 * @author ruoyi
 */
public interface TicketPayLogMapper
{
    public TicketPayLog selectTicketPayLogById(Long payId);

    public List<TicketPayLog> selectTicketPayLogList(TicketPayLog ticketPayLog);

    public int insertTicketPayLog(TicketPayLog ticketPayLog);

    public int updateTicketPayLog(TicketPayLog ticketPayLog);

    public int deleteTicketPayLogById(Long payId);

    public int deleteTicketPayLogByIds(Long[] payIds);
}
