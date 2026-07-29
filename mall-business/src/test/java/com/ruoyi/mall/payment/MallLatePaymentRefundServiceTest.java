package com.ruoyi.mall.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.payment.service.MallLatePaymentRefundService;

class MallLatePaymentRefundServiceTest
{
    @Mock private MallPaymentMapper paymentMapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private RefundPort refundPort;
    private MallLatePaymentRefundService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallLatePaymentRefundService(paymentMapper, orderMapper, refundPort);
    }

    @Test
    void refundsLatePaymentWithoutReopeningClosedOrder()
    {
        MallPayment payment = new MallPayment();
        payment.setPaymentId(11L); payment.setPaymentNo("PAY-1"); payment.setOrderId(1L);
        payment.setOrderNo("ORDER-1"); payment.setAmount(new BigDecimal("12.50"));
        payment.setStatus("REFUNDING");
        MallOrder order = new MallOrder();
        order.setOrderId(1L); order.setOrderNo("ORDER-1");
        order.setStatus("CLOSED"); order.setPaymentStatus("REFUNDING");
        when(paymentMapper.selectByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        when(orderMapper.selectByIdForUpdate(1L, null)).thenReturn(order);
        when(refundPort.refund("ORDER-1", "PAY-1", new BigDecimal("12.50")))
                .thenReturn(new RefundPort.RefundResult(true, "REFUND-1", "ok"));
        when(paymentMapper.markLatePaymentRefunded(11L, "REFUND-1")).thenReturn(1);
        when(orderMapper.markLatePaymentRefunded(1L)).thenReturn(1);
        when(orderMapper.insertOperationLog(any())).thenReturn(1);

        MallPayment result = service.refund("PAY-1");

        assertEquals("REFUNDED", result.getStatus());
        assertEquals("CLOSED", order.getStatus());
        verify(orderMapper).markLatePaymentRefunded(1L);
    }
}
