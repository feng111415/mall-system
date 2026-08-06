package com.ruoyi.mall.aftersale.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.aftersale.item.domain.dto.MallItemAfterSaleItemRequest;
import com.ruoyi.mall.aftersale.item.domain.dto.MallItemAfterSaleRequest;
import com.ruoyi.mall.aftersale.item.domain.dto.MallReturnTrackingRequest;
import com.ruoyi.mall.aftersale.item.mapper.MallItemAfterSaleMapper;
import com.ruoyi.mall.aftersale.item.service.MallItemAfterSaleApprovalStateService;
import com.ruoyi.mall.aftersale.item.service.MallItemAfterSaleService;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.payment.domain.MallPayment;

class MallItemAfterSaleServiceTest
{
    @Mock private MallItemAfterSaleMapper mapper;
    @Mock private MallOrderMapper orderMapper;
    @Mock private MallPaymentMapper paymentMapper;
    @Mock private RefundPort refundPort;
    @Mock private MemberMessagePort memberMessagePort;
    private MallItemAfterSaleService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallItemAfterSaleService(mapper, orderMapper,
                new MallItemAfterSaleApprovalStateService(mapper, paymentMapper), refundPort, memberMessagePort);
        when(mapper.insert(any())).thenAnswer(invocation -> { MallItemAfterSale value = invocation.getArgument(0); value.setAfterSaleId(20L); return 1; });
        when(mapper.insertItem(any())).thenReturn(1);
        when(mapper.selectActiveRequestedQuantity(101L)).thenReturn(0);
    }

    @Test
    void calculatesItemRefundAndShippingOnlyForFullOrder()
    {
        MallOrder order = order();
        MallOrderItem item = item(101L, 2, "19.90");
        when(orderMapper.selectByIdForUpdate(9L, 7L)).thenReturn(order);
        when(orderMapper.selectMemberOrderItems(9L, 7L)).thenReturn(List.of(item));
        MallItemAfterSaleRequest request = request("ONLY_REFUND", "OTHER", itemRequest(101L, 2));

        MallItemAfterSale result = service.apply(7L, 9L, request);

        assertEquals(new BigDecimal("39.80"), result.getRefundAmount());
        assertEquals(new BigDecimal("8.00"), result.getShippingRefundAmount());
        verify(mapper).insertItem(any());
        verify(memberMessagePort).publish(eq(7L), eq("AFTER_SALE"), eq("售后申请已提交"),
                eq("售后单 " + result.getAfterSaleNo() + " 等待审核"),
                eq("售后单 " + result.getAfterSaleNo() + " 等待审核"), eq("AFTER_SALE"),
                eq(20L), eq(result.getAfterSaleNo()), eq("/orders/9"));
    }

    @Test
    void rejectsQuantityBeyondRemainingQuantity()
    {
        when(orderMapper.selectByIdForUpdate(9L, 7L)).thenReturn(order());
        when(orderMapper.selectMemberOrderItems(9L, 7L)).thenReturn(List.of(item(101L, 2, "19.90")));
        MallItemAfterSaleRequest request = request("ONLY_REFUND", "OTHER", itemRequest(101L, 3));

        assertThrows(ServiceException.class, () -> service.apply(7L, 9L, request));
        verify(mapper, never()).insert(any());
    }

    @Test
    void rejectsDuplicateAlreadyOccupiedQuantity()
    {
        when(orderMapper.selectByIdForUpdate(9L, 7L)).thenReturn(order());
        when(orderMapper.selectMemberOrderItems(9L, 7L)).thenReturn(List.of(item(101L, 2, "19.90")));
        when(mapper.selectActiveRequestedQuantity(101L)).thenReturn(2);
        MallItemAfterSaleRequest request = request("ONLY_REFUND", "OTHER", itemRequest(101L, 1));

        assertThrows(ServiceException.class, () -> service.apply(7L, 9L, request));
        verify(mapper, never()).insert(any());
    }

    @Test
    void qualityIssueRequiresEvidence()
    {
        when(orderMapper.selectByIdForUpdate(9L, 7L)).thenReturn(order());
        when(orderMapper.selectMemberOrderItems(9L, 7L)).thenReturn(List.of(item(101L, 1, "19.90")));
        MallItemAfterSaleRequest request = request("RETURN_REFUND", "QUALITY", itemRequest(101L, 1));

        assertThrows(ServiceException.class, () -> service.apply(7L, 9L, request));
        verify(mapper, never()).insert(any());
    }

    @Test
    void rejectsOrderOutsideAfterSaleWindow()
    {
        MallOrder order = order();
        order.setPayTime(LocalDateTime.now().minusDays(8));
        when(orderMapper.selectByIdForUpdate(9L, 7L)).thenReturn(order);

        assertThrows(ServiceException.class,
                () -> service.apply(7L, 9L, request("ONLY_REFUND", "OTHER", itemRequest(101L, 1))));
        verify(orderMapper, never()).selectMemberOrderItems(any(), any());
        verify(mapper, never()).insert(any());
    }

    @Test
    void returnRefundMustSubmitTrackingBeforeRefund()
    {
        MallItemAfterSale afterSale = afterSale("RETURN_REFUND", "APPROVED");
        when(mapper.selectByIdForUpdate(20L)).thenReturn(afterSale);

        assertThrows(ServiceException.class, () -> service.approve(20L, "admin"));
        verify(refundPort, never()).refund(any(), any(), any(), any());
    }

    @Test
    void approvalAndOnlyRefundSuccessFollowStateMachine()
    {
        MallItemAfterSale pending = afterSale("ONLY_REFUND", "PENDING_REVIEW");
        MallItemAfterSale approved = afterSale("ONLY_REFUND", "APPROVED");
        MallItemAfterSale refunding = afterSale("ONLY_REFUND", "REFUNDING");
        MallPayment payment = new MallPayment();
        payment.setPaymentNo("PAY-9");
        when(mapper.selectByIdForUpdate(20L)).thenReturn(pending, approved, refunding);
        when(mapper.markApproved(20L)).thenReturn(1);
        when(mapper.markRefunding(20L)).thenReturn(1);
        when(mapper.markSuccess(20L, "CHANNEL-20")).thenReturn(1);
        when(paymentMapper.selectSuccessByOrderIdForUpdate(9L)).thenReturn(payment);
        when(refundPort.refund("AS-20", "ORDER-9", "PAY-9", new BigDecimal("27.80")))
                .thenReturn(new RefundPort.RefundResult(true, "CHANNEL-20", null));

        assertEquals("APPROVED", service.approveReview(20L).getStatus());
        assertEquals("SUCCESS", service.approve(20L, "admin").getStatus());
        verify(mapper).markSuccess(20L, "CHANNEL-20");
    }

    @Test
    void returnTrackingRejectsUnknownLogisticsCompany()
    {
        MallItemAfterSale approved = afterSale("RETURN_REFUND", "APPROVED");
        when(mapper.selectMemberByIdForUpdate(20L, 7L)).thenReturn(approved);
        MallReturnTrackingRequest request = new MallReturnTrackingRequest();
        request.setCompanyCode("FREE_TEXT");
        request.setTrackingNo("TRACK-20");

        assertThrows(ServiceException.class, () -> service.submitReturnTracking(7L, 20L, request));
        verify(mapper, never()).markReturnTracking(any(), any(), any());
    }

    @Test
    void fullQuantityQualityReturnIsNotMarkedMalicious()
    {
        when(orderMapper.selectByIdForUpdate(9L, 7L)).thenReturn(order());
        when(orderMapper.selectMemberOrderItems(9L, 7L)).thenReturn(List.of(item(101L, 1, "19.90")));
        MallItemAfterSaleRequest request = request("RETURN_REFUND", "QUALITY", itemRequest(101L, 1));
        request.setEvidenceUrl("https://example.test/evidence/1");

        MallItemAfterSale result = service.apply(7L, 9L, request);

        assertNull(result.getRiskSignal());
    }

    private MallItemAfterSaleRequest request(String type, String reasonCode, MallItemAfterSaleItemRequest item)
    {
        MallItemAfterSaleRequest value = new MallItemAfterSaleRequest(); value.setType(type); value.setReasonCode(reasonCode); value.setReason("商品问题"); value.setItems(List.of(item)); return value;
    }
    private MallItemAfterSaleItemRequest itemRequest(Long id, int quantity)
    { MallItemAfterSaleItemRequest value = new MallItemAfterSaleItemRequest(); value.setOrderItemId(id); value.setQuantity(quantity); return value; }
    private MallOrderItem item(Long id, int quantity, String price)
    { MallOrderItem value = new MallOrderItem(); value.setOrderItemId(id); value.setOrderId(9L); value.setSkuId(501L); value.setProductName("商品"); value.setSkuName("规格"); value.setQuantity(quantity); value.setUnitPrice(new BigDecimal(price)); value.setLineAmount(new BigDecimal(price).multiply(BigDecimal.valueOf(quantity))); return value; }
    private MallOrder order()
    { MallOrder value = new MallOrder(); value.setOrderId(9L); value.setOrderNo("ORDER-9"); value.setMemberId(7L); value.setStatus("COMPLETED"); value.setPaymentStatus("PAID"); value.setPayTime(LocalDateTime.now().minusDays(1)); value.setShippingFee(new BigDecimal("8.00")); return value; }

    private MallItemAfterSale afterSale(String type, String status)
    {
        MallItemAfterSale value = new MallItemAfterSale();
        value.setAfterSaleId(20L); value.setAfterSaleNo("AS-20"); value.setOrderId(9L);
        value.setOrderNo("ORDER-9"); value.setType(type); value.setStatus(status);
        value.setRefundAmount(new BigDecimal("19.80")); value.setShippingRefundAmount(new BigDecimal("8.00"));
        return value;
    }
}
