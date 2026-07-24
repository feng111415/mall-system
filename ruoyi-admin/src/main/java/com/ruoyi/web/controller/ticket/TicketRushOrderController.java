package com.ruoyi.web.controller.ticket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.ticket.domain.dto.TicketRushOrderRequest;
import com.ruoyi.ticket.service.ITicketRushOrderService;

@RestController
@RequestMapping("/ticket/rush")
public class TicketRushOrderController extends BaseController
{
    @Autowired
    private ITicketRushOrderService ticketRushOrderService;

    @PostMapping("/order")
    public AjaxResult createOrder(@RequestBody TicketRushOrderRequest request)
    {
        return success(ticketRushOrderService.createRushOrder(request));
    }
}
