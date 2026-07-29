package com.ruoyi.mall.order.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderStatus;
import com.ruoyi.mall.order.domain.MallOrderPaymentWindow;
import com.ruoyi.mall.order.mapper.MallOrderMapper;

@Service
public class MallOrderQueryService
{
    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private final MallOrderMapper mapper;

    public MallOrderQueryService(MallOrderMapper mapper)
    {
        this.mapper = mapper;
    }

    public List<MallOrder> list(Long memberId, String status, Integer limit, Integer offset)
    {
        requireMember(memberId);
        String normalizedStatus = normalizeStatus(status);
        int safeLimit = limit == null ? DEFAULT_LIMIT : Math.min(Math.max(limit, 1), MAX_LIMIT);
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        return mapper.selectMemberOrders(memberId, normalizedStatus, safeLimit, safeOffset);
    }

    public MallOrder detail(Long memberId, Long orderId)
    {
        requireMember(memberId);
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        MallOrder order = mapper.selectMemberOrder(orderId, memberId);
        if (order == null) throw new ServiceException("订单不存在");
        order.setItems(mapper.selectMemberOrderItems(orderId, memberId));
        order.setOperations(mapper.selectMemberOrderOperations(orderId, memberId));
        LocalDateTime now = LocalDateTime.now();
        order.setPaymentCreateDeadline(MallOrderPaymentWindow.createDeadline(order));
        order.setPaymentResultDeadline(MallOrderPaymentWindow.resultDeadline(order));
        boolean pending = MallOrderStatus.PENDING_PAYMENT.name().equals(order.getStatus());
        order.setCanCreatePayment(pending && "UNPAID".equals(order.getPaymentStatus())
                && MallOrderPaymentWindow.canCreatePayment(order, now));
        order.setCanConfirmPayment(pending && "PAYING".equals(order.getPaymentStatus())
                && !MallOrderPaymentWindow.isResultExpired(order, now));
        order.setCanCancel(pending && "UNPAID".equals(order.getPaymentStatus()));
        return order;
    }

    private String normalizeStatus(String status)
    {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) return null;
        try { return MallOrderStatus.valueOf(status.toUpperCase()).name(); }
        catch (IllegalArgumentException exception) { throw new ServiceException("订单状态无效"); }
    }

    private void requireMember(Long memberId)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
    }
}
