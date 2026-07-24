package com.ruoyi.mall.infrastructure.adapter.mock;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.ruoyi.mall.application.port.PaymentPort;

/**
 * 首期支付 Mock 适配器。
 *
 * <p>用于本地联调，不代表真实支付成功；正式环境应替换为具体验签的支付适配器。</p>
 */
@Component
public class MockPaymentAdapter implements PaymentPort
{
    @Override
    public PaymentCreateResult createPayment(String orderNo, BigDecimal amount, String subject)
    {
        String paymentNo = "MOCK-PAY-" + UUID.randomUUID();
        String paymentUrl = "/mock-payment/" + paymentNo;
        return new PaymentCreateResult(true, paymentNo, paymentUrl, "Mock 支付单创建成功");
    }
}
