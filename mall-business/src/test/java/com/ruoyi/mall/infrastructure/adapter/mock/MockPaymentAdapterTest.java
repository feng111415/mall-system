package com.ruoyi.mall.infrastructure.adapter.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import com.ruoyi.mall.application.port.PaymentPort;

class MockPaymentAdapterTest
{
    private final MockPaymentAdapter adapter = new MockPaymentAdapter();

    @Test
    void sameIdempotencyKeyReturnsStableProviderPaymentDetails()
    {
        PaymentPort.PaymentCreateResult first = adapter.createPayment(
                "PAY-1", "ORDER-1", new BigDecimal("12.50"), "订单");
        PaymentPort.PaymentCreateResult second = adapter.createPayment(
                "PAY-1", "ORDER-1", new BigDecimal("12.50"), "订单");

        assertEquals(first.paymentNo(), second.paymentNo());
        assertEquals(first.paymentUrl(), second.paymentUrl());
    }
}
