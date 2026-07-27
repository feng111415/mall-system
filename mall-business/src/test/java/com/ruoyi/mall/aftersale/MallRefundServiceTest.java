package com.ruoyi.mall.aftersale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.aftersale.domain.MallRefund;
import com.ruoyi.mall.aftersale.mapper.MallRefundMapper;
import com.ruoyi.mall.aftersale.service.MallRefundService;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

class MallRefundServiceTest
{
    @Mock private MallRefundMapper refundMapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private MallPaymentMapper paymentMapper;
    @Mock private RefundPort refundPort;
    private MallRefundService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallRefundService(refundMapper, orderMapper, paymentMapper, refundPort);
        when(orderMapper.insertOperationLog(any())).thenReturn(1);
    }

    @Test
    void memberAppliesOnceAndMovesOrderToAfterSale()
    {
        MallOrder order = order("SHIPPED", "PAID");
        when(orderMapper.selectByIdForUpdate(1L, 7L)).thenReturn(order);
        when(refundMapper.selectByOrderIdForUpdate(1L)).thenReturn(null);
        when(refundMapper.insert(any())).thenAnswer(invocation -> {
            MallRefund refund = invocation.getArgument(0);
            refund.setRefundId(11L);
            return 1;
        });
        when(orderMapper.markRefunding(1L, "SHIPPED")).thenReturn(1);

        MallRefund result = service.apply(7L, 1L, "商品有瑕疵");

        assertEquals("APPLIED", result.getStatus());
        assertEquals("SHIPPED", result.getOriginalOrderStatus());
        verify(orderMapper).markRefunding(1L, "SHIPPED");
        verify(orderMapper).insertOperationLog(any());
    }

    @Test
    void unpaidOrderCannotApplyRefund()
    {
        when(orderMapper.selectByIdForUpdate(1L, 7L)).thenReturn(order("PENDING_PAYMENT", "UNPAID"));
        when(refundMapper.selectByOrderIdForUpdate(1L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.apply(7L, 1L, "不想要了"));
        verify(refundMapper, never()).insert(any());
    }

    @Test
    void approveCallsRefundAdapterAndMarksOrderRefunded()
    {
        MallRefund refund = refund("APPLIED");
        MallOrder order = order("AFTER_SALE", "REFUNDING");
        MallPayment payment = new MallPayment(); payment.setPaymentNo("PAY-1");
        when(refundMapper.selectByIdForUpdate(11L)).thenReturn(refund);
        when(orderMapper.selectByIdForUpdate(1L, null)).thenReturn(order);
        when(refundMapper.markRefunding(11L)).thenReturn(1);
        when(paymentMapper.selectSuccessByOrderIdForUpdate(1L)).thenReturn(payment);
        when(refundPort.refund(anyString(), eq("PAY-1"), any())).thenReturn(
                new RefundPort.RefundResult(true, "REF-1", "ok"));
        when(refundMapper.markSuccess(11L, "REF-1")).thenReturn(1);
        when(orderMapper.markRefundSuccess(1L)).thenReturn(1);

        MallRefund result = service.approve(11L, "admin");

        assertEquals("SUCCESS", result.getStatus());
        verify(orderMapper).markRefundSuccess(1L);
        verify(orderMapper).insertOperationLog(any());
    }

    @Test
    void rejectRestoresOriginalOrderStatus()
    {
        MallRefund refund = refund("APPLIED");
        refund.setOriginalOrderStatus("COMPLETED");
        MallOrder order = order("AFTER_SALE", "REFUNDING");
        when(refundMapper.selectByIdForUpdate(11L)).thenReturn(refund);
        when(orderMapper.selectByIdForUpdate(1L, null)).thenReturn(order);
        when(refundMapper.markRejected(11L, "不符合售后条件")).thenReturn(1);
        when(orderMapper.restoreAfterRefundReject(1L, "COMPLETED")).thenReturn(1);

        MallRefund result = service.reject(11L, "admin", "不符合售后条件");

        assertEquals("REJECTED", result.getStatus());
        verify(orderMapper).restoreAfterRefundReject(1L, "COMPLETED");
    }

    private MallRefund refund(String status)
    {
        MallRefund refund = new MallRefund();
        refund.setRefundId(11L); refund.setOrderId(1L); refund.setOrderNo("M-1");
        refund.setMemberId(7L); refund.setRefundAmount(new BigDecimal("99.00"));
        refund.setStatus(status); refund.setOriginalOrderStatus("SHIPPED");
        return refund;
    }

    private MallOrder order(String status, String paymentStatus)
    {
        MallOrder order = new MallOrder();
        order.setOrderId(1L); order.setOrderNo("M-1"); order.setMemberId(7L);
        order.setStatus(status); order.setPaymentStatus(paymentStatus);
        order.setPayableAmount(new BigDecimal("99.00"));
        return order;
    }
}
