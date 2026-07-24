package com.ruoyi.ticket.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.ticket.domain.TicketActivity;
import com.ruoyi.ticket.domain.TicketOrder;
import com.ruoyi.ticket.domain.TicketOrderItem;
import com.ruoyi.ticket.domain.TicketStock;
import com.ruoyi.ticket.domain.TicketType;
import com.ruoyi.ticket.domain.dto.TicketRushOrderRequest;
import com.ruoyi.ticket.domain.dto.TicketRushPassengerRequest;
import com.ruoyi.ticket.domain.vo.TicketRushOrderResult;
import com.ruoyi.ticket.mapper.TicketActivityMapper;
import com.ruoyi.ticket.mapper.TicketOrderItemMapper;
import com.ruoyi.ticket.mapper.TicketOrderMapper;
import com.ruoyi.ticket.mapper.TicketStockMapper;
import com.ruoyi.ticket.mapper.TicketTypeMapper;
import com.ruoyi.ticket.service.ITicketRushOrderService;

@Service
public class TicketRushOrderServiceImpl implements ITicketRushOrderService
{
    @Autowired
    private TicketActivityMapper ticketActivityMapper;

    @Autowired
    private TicketTypeMapper ticketTypeMapper;

    @Autowired
    private TicketStockMapper ticketStockMapper;

    @Autowired
    private TicketOrderMapper ticketOrderMapper;

    @Autowired
    private TicketOrderItemMapper ticketOrderItemMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TicketRushOrderResult createRushOrder(TicketRushOrderRequest request)
    {
        validateRequest(request);

        TicketActivity activity = ticketActivityMapper.selectTicketActivityById(request.getActivityId());
        if (activity == null || !"1".equals(activity.getStatus()) || !"0".equals(activity.getDelFlag()))
        {
            throw new ServiceException("活动不可售");
        }

        Date now = DateUtils.getNowDate();
        if (activity.getSaleStartTime() != null && now.before(activity.getSaleStartTime()))
        {
            throw new ServiceException("活动尚未开售");
        }
        if (activity.getSaleEndTime() != null && now.after(activity.getSaleEndTime()))
        {
            throw new ServiceException("活动已停售");
        }

        TicketType type = ticketTypeMapper.selectTicketTypeById(request.getTypeId());
        if (type == null || !"0".equals(type.getStatus()))
        {
            throw new ServiceException("票种不可售");
        }
        if (!request.getActivityId().equals(type.getActivityId()) || !request.getSessionId().equals(type.getSessionId()))
        {
            throw new ServiceException("票种与活动场次不匹配");
        }

        int quantity = request.getQuantity();
        if (type.getMinBuy() != null && quantity < type.getMinBuy())
        {
            throw new ServiceException("购买数量低于最小限制");
        }
        if (type.getMaxBuy() != null && quantity > type.getMaxBuy())
        {
            throw new ServiceException("购买数量超过票种限制");
        }
        if (activity.getPurchaseLimit() != null && activity.getPurchaseLimit() > 0)
        {
            int effective = ticketOrderMapper.countEffectiveQuantity(request.getUserId(), request.getActivityId());
            if (effective + quantity > activity.getPurchaseLimit())
            {
                throw new ServiceException("超过活动限购数量");
            }
        }
        if (ticketOrderMapper.countUnpaidOrder(request.getUserId(), request.getActivityId()) > 0)
        {
            throw new ServiceException("存在待支付订单，请先处理");
        }

        int locked = ticketStockMapper.lockStockByTypeId(request.getTypeId(), quantity);
        if (locked != 1)
        {
            throw new ServiceException("库存不足");
        }

        BigDecimal totalAmount = type.getPrice().multiply(BigDecimal.valueOf(quantity));
        Date expireTime = buildExpireTime(activity.getOrderTimeout());
        String orderNo = buildOrderNo();

        TicketOrder order = new TicketOrder();
        order.setOrderNo(orderNo);
        order.setUserId(request.getUserId());
        order.setOpenid(request.getOpenid());
        order.setActivityId(request.getActivityId());
        order.setSessionId(request.getSessionId());
        order.setOrderTitle(activity.getActivityName() + " - " + type.getTypeName());
        order.setTotalQuantity(quantity);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPayAmount(totalAmount);
        order.setPayStatus("0");
        order.setOrderStatus("0");
        order.setSource("0");
        order.setClientIp(request.getClientIp());
        order.setExpireTime(expireTime);
        order.setCreateBy("rush");
        ticketOrderMapper.insertTicketOrder(order);

        createOrderItems(order, type, request);

        TicketStock stock = ticketStockMapper.selectTicketStockByTypeId(request.getTypeId());
        if (stock != null)
        {
            type.setLockedStock(stock.getLockedStock());
            type.setSoldStock(stock.getSoldStock());
            type.setTotalStock(stock.getTotalStock());
            ticketTypeMapper.updateTicketType(type);
        }

        TicketRushOrderResult result = new TicketRushOrderResult();
        result.setOrderId(order.getOrderId());
        result.setOrderNo(order.getOrderNo());
        result.setPayAmount(order.getPayAmount());
        result.setExpireTime(order.getExpireTime());
        result.setOrderStatus(order.getOrderStatus());
        return result;
    }

    private void validateRequest(TicketRushOrderRequest request)
    {
        if (request == null || request.getActivityId() == null || request.getSessionId() == null || request.getTypeId() == null)
        {
            throw new ServiceException("抢票参数不完整");
        }
        if (request.getUserId() == null || StringUtils.isEmpty(request.getOpenid()))
        {
            throw new ServiceException("用户信息不完整");
        }
        if (request.getQuantity() == null || request.getQuantity() <= 0)
        {
            throw new ServiceException("购买数量无效");
        }
        List<TicketRushPassengerRequest> passengers = request.getPassengers();
        if (passengers != null && !passengers.isEmpty() && passengers.size() != request.getQuantity())
        {
            throw new ServiceException("购票人数量必须与购买数量一致");
        }
    }

    private Date buildExpireTime(Integer timeout)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtils.getNowDate());
        calendar.add(Calendar.SECOND, timeout == null || timeout <= 0 ? 900 : timeout);
        return calendar.getTime();
    }

    private String buildOrderNo()
    {
        return "T" + DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 8);
    }

    private void createOrderItems(TicketOrder order, TicketType type, TicketRushOrderRequest request)
    {
        List<TicketRushPassengerRequest> passengers = request.getPassengers();
        if (passengers == null || passengers.isEmpty())
        {
            insertOrderItem(order, type, null, request.getQuantity());
            return;
        }
        for (TicketRushPassengerRequest passenger : passengers)
        {
            insertOrderItem(order, type, passenger, 1);
        }
    }

    private void insertOrderItem(TicketOrder order, TicketType type, TicketRushPassengerRequest passenger, Integer quantity)
    {
        TicketOrderItem item = new TicketOrderItem();
        item.setOrderId(order.getOrderId());
        item.setOrderNo(order.getOrderNo());
        item.setUserId(order.getUserId());
        item.setActivityId(order.getActivityId());
        item.setSessionId(order.getSessionId());
        item.setTypeId(type.getTypeId());
        item.setTypeName(type.getTypeName());
        item.setTicketNo("TK" + DateUtils.dateTimeNow() + IdUtils.fastSimpleUUID().substring(0, 8));
        if (passenger != null)
        {
            item.setPassengerId(passenger.getPassengerId());
            item.setPassengerName(passenger.getPassengerName());
            item.setIdType(passenger.getIdType());
            item.setIdNo(passenger.getIdNo());
        }
        item.setPrice(type.getPrice());
        item.setQuantity(quantity);
        item.setItemAmount(type.getPrice().multiply(BigDecimal.valueOf(quantity)));
        item.setItemStatus("0");
        ticketOrderItemMapper.insertTicketOrderItem(item);
    }
}
