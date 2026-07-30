package com.ruoyi.mall.infrastructure.adapter.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.ruoyi.mall.application.port.RefundPort;

class MockRefundAdapterTest
{
    private final MockRefundAdapter adapter = new MockRefundAdapter();

    @Test
    void sameIdempotencyKeyReturnsStableProviderRefundNumber()
    {
        RefundPort.RefundResult first = adapter.refund(
                "REFUND-1", "ORDER-1", "PAY-1", new BigDecimal("12.50"));
        RefundPort.RefundResult second = adapter.refund(
                "REFUND-1", "ORDER-1", "PAY-1", new BigDecimal("12.50"));

        assertEquals(first.providerRefundNo(), second.providerRefundNo());
    }
}
