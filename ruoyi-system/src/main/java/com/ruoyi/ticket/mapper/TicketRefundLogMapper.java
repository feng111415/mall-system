package com.ruoyi.ticket.mapper;

import java.util.List;
import com.ruoyi.ticket.domain.TicketRefundLog;

/**
 * 票务退款日志Mapper接口
 *
 * @author ruoyi
 */
public interface TicketRefundLogMapper
{
    public TicketRefundLog selectTicketRefundLogById(Long refundId);

    public List<TicketRefundLog> selectTicketRefundLogList(TicketRefundLog ticketRefundLog);

    public int insertTicketRefundLog(TicketRefundLog ticketRefundLog);

    public int updateTicketRefundLog(TicketRefundLog ticketRefundLog);

    public int deleteTicketRefundLogById(Long refundId);

    public int deleteTicketRefundLogByIds(Long[] refundIds);
}
