package com.ruoyi.mall.infrastructure.adapter.mock;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Component;
import com.ruoyi.mall.application.port.RefundPort;

/** 本地联调用 Mock 退款适配器。 */
@Component
public class MockRefundAdapter implements RefundPort
{
    @Override
    public RefundResult refund(String orderNo, String paymentNo, BigDecimal amount)
    {
        return new RefundResult(true, "MOCK-REFUND-" + UUID.randomUUID().toString().replace("-", ""),
                "Mock 退款成功");
    }
}
