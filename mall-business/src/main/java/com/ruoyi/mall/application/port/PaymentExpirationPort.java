package com.ruoyi.mall.application.port;

/** 订单超时流程关闭未完成支付单的跨模块接口。 */
public interface PaymentExpirationPort
{
    boolean closePendingPayment(Long orderId);
}
