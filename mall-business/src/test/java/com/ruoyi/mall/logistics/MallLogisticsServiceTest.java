package com.ruoyi.mall.logistics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.dto.MallLogisticsNodeRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentCaptor;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.LogisticsPort;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.logistics.domain.MallFulfillmentOrder;
import com.ruoyi.mall.logistics.domain.MallLogisticsNodeStatus;
import com.ruoyi.mall.logistics.domain.MallFulfillmentQuery;
import com.ruoyi.mall.logistics.mapper.MallLogisticsMapper;
import com.ruoyi.mall.logistics.service.MallLogisticsService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;

class MallLogisticsServiceTest
{
    @Mock private MallLogisticsMapper logisticsMapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private LogisticsPort logisticsPort;
    @Mock private MemberMessagePort memberMessagePort;
    private MallLogisticsService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallLogisticsService(logisticsMapper, orderMapper, logisticsPort, memberMessagePort);
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
        when(logisticsPort.createShipment(eq(order.getOrderNo()), eq("MOCK"), eq("TRACK-1"), any(), any()))
                .thenReturn(new LogisticsPort.LogisticsCreateResult(true, "MOCK-TRACK-1", "ok"));
        when(orderMapper.markShipped(9L)).thenReturn(1);

        MallLogisticsShipment result = service.shipByAdmin(9L, "MOCK", "模拟物流", "TRACK-1", "admin");

        assertEquals("MOCK-TRACK-1", result.getTrackingNo());
        assertEquals("IN_TRANSIT", result.getStatus());
        verify(orderMapper).markShipped(9L);
        verify(logisticsMapper).insertNode(any());
        verify(orderMapper).insertOperationLog(any());
        verify(memberMessagePort).publish(eq(7L), eq("LOGISTICS"), eq("物流已发货"),
                eq("订单 " + order.getOrderNo() + " 已发货，运单号 MOCK-TRACK-1"),
                eq("订单 " + order.getOrderNo() + " 已发货，运单号 MOCK-TRACK-1"), eq("LOGISTICS"),
                eq(9L), eq(order.getOrderNo()), eq("/orders/9/logistics"));
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

        assertEquals(existing, service.shipByAdmin(9L, "MOCK", "模拟物流", "TRACK-1", "admin"));
        verify(logisticsPort, never()).createShipment(any(), any(), any(), any(), any());
        verify(orderMapper, never()).markShipped(any());
    }

    @Test
    void unpaidOrderCannotBeShipped()
    {
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order("PENDING_PAYMENT", "UNPAID"));
        when(logisticsMapper.selectByOrderIdForUpdate(9L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.shipByAdmin(9L, "MOCK", "模拟物流", "TRACK-1", "admin"));
        verify(logisticsPort, never()).createShipment(any(), any(), any(), any(), any());
    }

    @Test
    void rejectsUnknownLogisticsCompany()
    {
        MallOrder order = order("PENDING_SHIPMENT", "PAID");
        when(orderMapper.selectByIdForUpdate(9L, null)).thenReturn(order);
        when(logisticsMapper.selectByOrderIdForUpdate(9L)).thenReturn(null);

        assertThrows(ServiceException.class,
                () -> service.shipByAdmin(9L, "UNKNOWN", "任意名称", "TRACK-1", "admin"));
        verify(logisticsPort, never()).createShipment(any(), any(), any(), any(), any());
    }

    @Test
    void memberCanOnlyQueryOwnShipment()
    {
        when(logisticsMapper.selectMemberShipment(9L, 7L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.detailForMember(7L, 9L));
    }

    @Test
    void appendsOnlyAllowedChronologicalNode()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment(); shipment.setShipmentId(11L); shipment.setTrackingNo("TRACK-1");
        MallLogisticsNode latest = new MallLogisticsNode(); latest.setEventTime(LocalDateTime.of(2026, 7, 30, 10, 0));
        when(logisticsMapper.selectByIdForUpdate(11L)).thenReturn(shipment);
        when(logisticsMapper.selectLatestNode(11L)).thenReturn(latest);
        when(logisticsMapper.insertNode(any())).thenReturn(1);
        when(logisticsMapper.selectNodes(11L)).thenReturn(java.util.List.of());
        MallLogisticsNodeRequest request = new MallLogisticsNodeRequest(); request.setNodeStatus("IN_TRANSIT");
        request.setTitle("运输中"); request.setDescription("包裹已离开仓库"); request.setEventTime("2026-07-30T11:00:00");

        assertEquals(shipment, service.appendNode(11L, request, "admin"));
        verify(logisticsMapper).insertNode(any());
        request.setEventTime("2026-07-30T09:00:00");
        assertThrows(ServiceException.class, () -> service.appendNode(11L, request, "admin"));
    }

    @Test
    void clampsDefaultNodeTimeWhenServerClockMovesBackward()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setShipmentId(11L); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        shipment.setTrackingNo("TRACK-1"); shipment.setStatus("IN_TRANSIT");
        MallLogisticsNode latest = new MallLogisticsNode();
        latest.setNodeStatus("IN_TRANSIT"); latest.setEventTime(LocalDateTime.now().plusMinutes(1));
        when(logisticsMapper.selectByIdForUpdate(11L)).thenReturn(shipment);
        when(logisticsMapper.selectLatestNode(11L)).thenReturn(latest);
        when(logisticsMapper.selectNodes(11L)).thenReturn(java.util.List.of(latest));
        MallLogisticsNodeRequest request = new MallLogisticsNodeRequest();
        request.setNodeStatus("OUT_FOR_DELIVERY"); request.setTitle("派送中");
        request.setDescription("服务器时钟发生回拨");

        assertEquals(shipment, service.appendNode(11L, request, "admin"));

        ArgumentCaptor<MallLogisticsNode> captor = ArgumentCaptor.forClass(MallLogisticsNode.class);
        verify(logisticsMapper).insertNode(captor.capture());
        assertFalse(captor.getValue().getEventTime().isBefore(latest.getEventTime()));
    }

    @Test
    void cannotAppendNodeAfterDelivered()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setShipmentId(11L); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        shipment.setTrackingNo("TRACK-1"); shipment.setStatus("DELIVERED");
        MallLogisticsNode latest = new MallLogisticsNode();
        latest.setNodeStatus("DELIVERED"); latest.setEventTime(LocalDateTime.of(2026, 7, 30, 10, 0));
        when(logisticsMapper.selectByIdForUpdate(11L)).thenReturn(shipment);
        when(logisticsMapper.selectLatestNode(11L)).thenReturn(latest);
        MallLogisticsNodeRequest request = new MallLogisticsNodeRequest();
        request.setNodeStatus("IN_TRANSIT"); request.setTitle("运输中"); request.setDescription("包裹仍在运输");
        request.setEventTime("2026-07-30T11:00:00");

        assertThrows(ServiceException.class, () -> service.appendNode(11L, request, "admin"));
        verify(logisticsMapper, never()).insertNode(any());
        verify(orderMapper, never()).insertOperationLog(any());
    }

    @Test
    void arrivedNodeDoesNotMarkShipmentDelivered()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setShipmentId(11L); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        shipment.setTrackingNo("TRACK-1"); shipment.setStatus("IN_TRANSIT");
        MallLogisticsNode latest = new MallLogisticsNode();
        latest.setNodeStatus("OUT_FOR_DELIVERY"); latest.setEventTime(LocalDateTime.of(2026, 7, 30, 10, 0));
        when(logisticsMapper.selectByIdForUpdate(11L)).thenReturn(shipment);
        when(logisticsMapper.selectLatestNode(11L)).thenReturn(latest);
        when(logisticsMapper.insertNode(any())).thenReturn(1);
        when(logisticsMapper.selectNodes(11L)).thenReturn(java.util.List.of(latest));
        MallLogisticsNodeRequest request = new MallLogisticsNodeRequest();
        request.setNodeStatus("ARRIVED"); request.setTitle("已送达"); request.setDescription("包裹已送达收件点");
        request.setEventTime("2026-07-30T11:00:00");

        assertEquals(shipment, service.appendNode(11L, request, "admin"));
        assertEquals("IN_TRANSIT", shipment.getStatus());
        verify(logisticsMapper, never()).updateShipmentDelivered(any(), any());
    }

    @Test
    void allowsCorrectionAfterDeliveredWithoutChangingReceiptState()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setShipmentId(11L); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        shipment.setTrackingNo("TRACK-1"); shipment.setStatus("DELIVERED");
        MallLogisticsNode latest = new MallLogisticsNode();
        latest.setNodeStatus("DELIVERED"); latest.setEventTime(LocalDateTime.of(2026, 7, 30, 10, 0));
        when(logisticsMapper.selectByIdForUpdate(11L)).thenReturn(shipment);
        when(logisticsMapper.selectLatestNode(11L)).thenReturn(latest);
        when(logisticsMapper.insertNode(any())).thenReturn(1);
        when(logisticsMapper.selectNodes(11L)).thenReturn(java.util.List.of(latest));
        MallLogisticsNodeRequest request = new MallLogisticsNodeRequest();
        request.setNodeStatus("CORRECTION"); request.setTitle("鏇存"); request.setDescription("鏇存璇存槑");
        request.setEventTime("2026-07-30T11:00:00");

        assertEquals(shipment, service.appendNode(11L, request, "admin"));
        verify(logisticsMapper).insertNode(any());
    }

    @Test
    void memberCanConfirmOnlySignedShipment()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment(); shipment.setShipmentId(11L); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        MallLogisticsNode delivered = new MallLogisticsNode(); delivered.setNodeStatus("DELIVERED"); delivered.setEventTime(LocalDateTime.now());
        shipment.setStatus("DELIVERED");
        when(logisticsMapper.selectMemberShipment(9L, 7L)).thenReturn(shipment);
        when(logisticsMapper.selectLatestNode(11L)).thenReturn(delivered);
        when(orderMapper.markCompleted(9L)).thenReturn(1);
        when(logisticsMapper.selectNodes(11L)).thenReturn(java.util.List.of(delivered));
        assertEquals(shipment, service.confirmReceipt(7L, 9L));
        verify(orderMapper).markCompleted(9L);
        verify(orderMapper).insertOperationLog(any());
        verify(logisticsMapper, never()).selectLatestNode(11L);
    }

    @Test
    void memberCannotConfirmArrivedShipment()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setShipmentId(11L); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        shipment.setStatus("IN_TRANSIT");
        when(logisticsMapper.selectMemberShipment(9L, 7L)).thenReturn(shipment);

        assertThrows(ServiceException.class, () -> service.confirmReceipt(7L, 9L));
        verify(orderMapper, never()).markCompleted(any());
        verify(orderMapper, never()).insertOperationLog(any());
    }

    @Test
    void normalizesWorkbenchFiltersAndReadsPageFromMapper()
    {
        MallFulfillmentQuery query = new MallFulfillmentQuery();
        query.setOrderNo("  ORDER-9  "); query.setReceiverKeyword("  张三 ");
        query.setWorkflowStatus("invalid"); query.setAttentionType("DATA_INCONSISTENT");
        when(logisticsMapper.selectFulfillmentOrders(any())).thenReturn(java.util.List.of(new MallFulfillmentOrder()));

        assertEquals(1, service.listForAdmin(query).size());
        verify(logisticsMapper).selectFulfillmentOrders(query);
        assertEquals("ORDER-9", query.getOrderNo());
        assertEquals("张三", query.getReceiverKeyword());
        assertEquals(null, query.getWorkflowStatus());
        assertEquals("DATA_INCONSISTENT", query.getAttentionType());
    }

    @Test
    void exposesServerCompanyWhitelistWithoutMockProvider()
    {
        assertEquals(false, service.listCompanies().stream().anyMatch(item -> "MOCK".equals(item.getCode())));
        assertEquals(true, service.listCompanies().stream().anyMatch(item -> "SF".equals(item.getCode())));
        assertEquals(MallLogisticsNodeStatus.DELIVERED.getLabel(), MallLogisticsNodeStatus.parse(" delivered ").getLabel());
    }

    @Test
    void autoConfirmsDeliveredOrdersAfterSevenDays()
    {
        MallLogisticsShipment shipment = new MallLogisticsShipment(); shipment.setOrderId(9L); shipment.setOrderNo("ORDER-9");
        when(logisticsMapper.selectDeliveredBefore(any(LocalDateTime.class))).thenReturn(java.util.List.of(shipment));
        when(orderMapper.markCompleted(9L)).thenReturn(1);
        assertEquals(1, service.autoConfirmReceipts());
        verify(orderMapper).insertOperationLog(any());
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
