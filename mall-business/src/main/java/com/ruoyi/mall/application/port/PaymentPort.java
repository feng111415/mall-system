package com.ruoyi.mall.application.port;

import java.math.BigDecimal;

/**
 * 支付服务出站端口。
 *
 * <p>支付渠道由具体适配器实现，订单业务不直接依赖第三方 SDK。</p>
 */
public interface PaymentPort
{
    /**
     * 创建支付单。
     *
     * @param orderNo 商城订单号
     * @param amount 支付金额
     * @param subject 订单标题
     * @return 支付单创建结果
     */
    PaymentCreateResult createPayment(String orderNo, BigDecimal amount, String subject);

    /** 支付单创建结果。 */
    record PaymentCreateResult(boolean success, String paymentNo, String paymentUrl, String message)
    {
    }
}
