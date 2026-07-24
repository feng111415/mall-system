package com.ruoyi.ticket.service;

import java.util.List;
import com.ruoyi.ticket.domain.TicketRefundLog;

/**
 * 票务退款日志Service接口
 *
 * @author ruoyi
 */
public interface ITicketRefundLogService
{
    public TicketRefundLog selectTicketRefundLogById(Long refundId);

    public List<TicketRefundLog> selectTicketRefundLogList(TicketRefundLog ticketRefundLog);

    public int insertTicketRefundLog(TicketRefundLog ticketRefundLog);

    public int updateTicketRefundLog(TicketRefundLog ticketRefundLog);

    public int deleteTicketRefundLogByIds(Long[] refundIds);

    public int deleteTicketRefundLogById(Long refundId);
}
