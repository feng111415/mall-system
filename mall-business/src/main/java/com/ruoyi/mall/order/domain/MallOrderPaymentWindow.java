package com.ruoyi.mall.order.domain;

import java.time.LocalDateTime;

/** 订单支付创建和结果等待期限的唯一规则来源。 */
public final class MallOrderPaymentWindow
{
    public static final int CREATE_MINUTES = 30;
    public static final int RESULT_MINUTES = 35;

    private MallOrderPaymentWindow() { }

    public static LocalDateTime createDeadline(MallOrder order)
    {
        return order == null || order.getCreateTime() == null
                ? null : order.getCreateTime().plusMinutes(CREATE_MINUTES);
    }

    public static LocalDateTime resultDeadline(MallOrder order)
    {
        return order == null || order.getCreateTime() == null
                ? null : order.getCreateTime().plusMinutes(RESULT_MINUTES);
    }

    public static boolean canCreatePayment(MallOrder order, LocalDateTime now)
    {
        LocalDateTime deadline = createDeadline(order);
        return deadline != null && now != null && now.isBefore(deadline);
    }

    public static boolean isResultExpired(MallOrder order, LocalDateTime now)
    {
        LocalDateTime deadline = resultDeadline(order);
        return deadline == null || now == null || !now.isBefore(deadline);
    }
}
