package com.ruoyi.mall.logistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.LogisticsPort;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.logistics.mapper.MallLogisticsMapper;
import com.ruoyi.mall.logistics.service.MallLogisticsService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;

class MallLogisticsServiceTest
{
    @Mock private MallLogisticsMapper logisticsMapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private LogisticsPort logisticsPort;
    private MallLogisticsService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallLogisticsService(logisticsMapper, orderMapper, logisticsPort);
        when(logisticsMapper.insertShipment(any())).thenAnswer(invocation -> {
            MallLogisticsShipment value = invocation.getArgument(0);
            value.setShipmentId(11L);
            return 1;
        });
        when(logisticsMapper.insertNode(any())).thenReturn(1);
        when(orderMapper.insertOperationLog(any())).thenReturn(1);
    }

    @Test
    void shipsPaidPendingOrderAndWritesTrackingNode()
    {
        MallOrder order = order("PENDING_SHIPMENT", "PAID");
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order);
        when(logisticsMapper.selectByOrderIdForUpdate(9L)).thenReturn(null);
        when(logisticsPort.createShipment(eq(order.getOrderNo()), eq("MOCK"), any(), any()))
                .thenReturn(new LogisticsPort.LogisticsCreateResult(true, "MOCK-TRACK-1", "ok"));
        when(orderMapper.markShipped(9L)).thenReturn(1);

        MallLogisticsShipment result = service.shipByAdmin(9L, null, "admin");

        assertEquals("MOCK-TRACK-1", result.getTrackingNo());
        assertEquals("IN_TRANSIT", result.getStatus());
        verify(orderMapper).markShipped(9L);
        verify(logisticsMapper).insertNode(any());
        verify(orderMapper).insertOperationLog(any());
    }

    @Test
    void repeatedShippingReturnsExistingShipmentWithoutCallingProvider()
    {
        MallOrder order = order("PENDING_SHIPMENT", "PAID");
        MallLogisticsShipment existing = new MallLogisticsShipment();
        existing.setShipmentId(12L); existing.setTrackingNo("MOCK-EXISTING");
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order);
        when(logisticsMapper.selectByOrderIdForUpdate(9L)).thenReturn(existing);
        when(logisticsMapper.selectNodes(12L)).thenReturn(java.util.List.of());

        assertEquals(existing, service.shipByAdmin(9L, "MOCK", "admin"));
        verify(logisticsPort, never()).createShipment(any(), any(), any(), any());
        verify(orderMapper, never()).markShipped(any());
    }

    @Test
    void unpaidOrderCannotBeShipped()
    {
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order("PENDING_PAYMENT", "UNPAID"));
        when(logisticsMapper.selectByOrderIdForUpdate(9L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.shipByAdmin(9L, "MOCK", "admin"));
        verify(logisticsPort, never()).createShipment(any(), any(), any(), any());
    }

    @Test
    void memberCanOnlyQueryOwnShipment()
    {
        when(logisticsMapper.selectMemberShipment(9L, 7L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.detailForMember(7L, 9L));
    }

    private MallOrder order(String status, String paymentStatus)
    {
        MallOrder order = new MallOrder();
        order.setOrderId(9L); order.setOrderNo("M20260727150000000009"); order.setMemberId(7L);
        order.setStatus(status); order.setPaymentStatus(paymentStatus);
        order.setReceiverName("张三"); order.setReceiverProvince("广东省"); order.setReceiverCity("深圳市");
        order.setReceiverDistrict("南山区"); order.setReceiverDetailAddress("科技园");
        return order;
    }
}
