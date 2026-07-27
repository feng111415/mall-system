package com.ruoyi.mall.application.port;

import java.math.BigDecimal;

/** 退款渠道出站端口，隔离微信/支付宝等第三方退款 SDK。 */
public interface RefundPort
{
    RefundResult refund(String orderNo, String paymentNo, BigDecimal amount);

    record RefundResult(boolean success, String providerRefundNo, String message) { }
}
