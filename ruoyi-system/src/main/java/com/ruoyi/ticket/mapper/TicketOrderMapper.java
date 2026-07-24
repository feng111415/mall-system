package com.ruoyi.ticket.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.ticket.domain.TicketOrder;

/**
 * 票务订单Mapper接口
 *
 * @author ruoyi
 */
public interface TicketOrderMapper
{
    public TicketOrder selectTicketOrderById(Long orderId);

    public List<TicketOrder> selectTicketOrderList(TicketOrder ticketOrder);

    public int insertTicketOrder(TicketOrder ticketOrder);

    public int updateTicketOrder(TicketOrder ticketOrder);

    public int deleteTicketOrderById(Long orderId);

    public int deleteTicketOrderByIds(Long[] orderIds);

    public int countUnpaidOrder(@Param("userId") Long userId, @Param("activityId") Long activityId);

    public int countEffectiveQuantity(@Param("userId") Long userId, @Param("activityId") Long activityId);
}
