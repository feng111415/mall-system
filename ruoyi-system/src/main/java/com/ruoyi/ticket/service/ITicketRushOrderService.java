package com.ruoyi.ticket.service;

import com.ruoyi.ticket.domain.dto.TicketRushOrderRequest;
import com.ruoyi.ticket.domain.vo.TicketRushOrderResult;

public interface ITicketRushOrderService
{
    public TicketRushOrderResult createRushOrder(TicketRushOrderRequest request);
}
