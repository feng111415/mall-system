package com.ruoyi.mall.governance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.aftersale.domain.MallRefund;
import com.ruoyi.mall.aftersale.mapper.MallRefundMapper;
import com.ruoyi.mall.governance.domain.MallCompensationTask;
import com.ruoyi.mall.governance.mapper.MallCompensationMapper;
import com.ruoyi.mall.governance.service.MallCompensationService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.payment.service.MallLatePaymentRefundService;

class MallCompensationServiceTest
{
    @Mock private MallCompensationMapper mapper;
    @Mock private InventoryPort inventoryPort;
    @Mock private MallPaymentMapper paymentMapper;
    @Mock private MallRefundMapper refundMapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private MallLatePaymentRefundService latePaymentRefundService;
    private MallCompensationService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallCompensationService(mapper, inventoryPort);
    }

    @Test
    void taskCreationIsIdempotentByBusinessKey()
    {
        MallCompensationTask existing = task("INVENTORY_RELEASE", "ORDER-1", "SUCCESS");
        when(mapper.selectByBusinessKey("INVENTORY_RELEASE", "ORDER-1")).thenReturn(existing);

        assertEquals(existing, service.create("INVENTORY_RELEASE", "ORDER-1", "ORDER-1", null));
        verify(mapper, never()).insertIgnore(any());
    }

    @Test
    void dueInventoryTaskRunsOnceAndMarksSuccess()
    {
        MallCompensationTask task = task("INVENTORY_RELEASE", "ORDER-1", "PENDING");
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1);
        when(mapper.markSuccess(1L)).thenReturn(1);

        assertEquals(1, service.runDueTasks());
        verify(inventoryPort).release("ORDER-1");
        verify(mapper).markSuccess(1L);
    }

    @Test
    void unsupportedTaskMovesToManualAfterMaxRetry()
    {
        MallCompensationTask task = task("PAYMENT_RECONCILE", "PAY-1", "PENDING");
        task.setMaxRetries(1); task.setRetryCount(0);
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1);

        assertEquals(1, service.runDueTasks());
        verify(mapper).markFailure(eq(1L), eq("MANUAL"), any(), any());
    }

    @Test
    void paymentTaskConfirmsPaymentAndInventoryWithoutRepeatingSuccess()
    {
        MallCompensationService fullService = new MallCompensationService(mapper, inventoryPort,
                paymentMapper, refundMapper, orderMapper, latePaymentRefundService);
        MallCompensationTask task = task("PAYMENT_CONFIRM", "PAY-1", "PENDING");
        MallPayment payment = new MallPayment(); payment.setPaymentId(3L); payment.setPaymentNo("PAY-1");
        payment.setOrderId(9L); payment.setStatus("PAYING");
        MallOrder order = new MallOrder(); order.setOrderId(9L); order.setOrderNo("ORDER-1");
        order.setStatus("PENDING_PAYMENT"); order.setPaymentStatus("PAYING");
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1); when(mapper.markSuccess(1L)).thenReturn(1);
        when(paymentMapper.selectByPaymentNoForUpdate("PAY-1")).thenReturn(payment);
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order);
        when(orderMapper.markPaymentSuccess(9L)).thenReturn(1); when(paymentMapper.updateSuccess(3L)).thenReturn(1);

        assertEquals(1, fullService.runDueTasks());
        verify(inventoryPort).confirm("ORDER-1", true);
    }

    @Test
    void refundTaskOnlyFinalizesWhenProviderRefundNumberExists()
    {
        MallCompensationService fullService = new MallCompensationService(mapper, inventoryPort,
                paymentMapper, refundMapper, orderMapper, latePaymentRefundService);
        MallCompensationTask task = task("REFUND_CONFIRM", "REF-1", "PENDING");
        MallRefund refund = new MallRefund(); refund.setRefundId(4L); refund.setRefundNo("REF-1");
        refund.setOrderId(9L); refund.setStatus("REFUNDING"); refund.setProviderRefundNo("PROVIDER-1");
        MallOrder order = new MallOrder(); order.setOrderId(9L); order.setStatus("AFTER_SALE"); order.setPaymentStatus("REFUNDING");
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1); when(mapper.markSuccess(1L)).thenReturn(1);
        when(refundMapper.selectByRefundNoForUpdate("REF-1")).thenReturn(refund);
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order);
        when(refundMapper.markSuccess(4L, "PROVIDER-1")).thenReturn(1); when(orderMapper.markRefundSuccess(9L)).thenReturn(1);

        assertEquals(1, fullService.runDueTasks());
        verify(orderMapper).markRefundSuccess(9L);
    }

    @Test
    void latePaymentRefundTaskUsesRegisteredRefundHandler()
    {
        MallCompensationService fullService = new MallCompensationService(mapper, inventoryPort,
                paymentMapper, refundMapper, orderMapper, latePaymentRefundService);
        MallCompensationTask task = task("LATE_PAYMENT_REFUND", "PAY-1", "PENDING");
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1);
        when(mapper.markSuccess(1L)).thenReturn(1);

        assertEquals(1, fullService.runDueTasks());

        verify(latePaymentRefundService).refund("PAY-1");
        verify(mapper).markSuccess(1L);
    }

    private MallCompensationTask task(String type, String key, String status)
    {
        MallCompensationTask task = new MallCompensationTask();
        task.setTaskId(1L); task.setTaskType(type); task.setBusinessKey(key);
        task.setStatus(status); task.setRetryCount(0); task.setMaxRetries(5);
        return task;
    }
}
