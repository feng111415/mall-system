package com.ruoyi.mall.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
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
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.cart.domain.MallCartResult;
import com.ruoyi.mall.cart.service.IMallCartService;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.dto.MallCreateOrderRequest;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.order.service.MallOrderCreateService;

class MallOrderCreateServiceTest
{
    @Mock private MallOrderMapper mapper;
    @Mock private IMallCartService cartService;
    @Mock private MallMemberAuthService memberService;
    @Mock private InventoryPort inventoryPort;
    @Mock private MemberMessagePort memberMessagePort;
    private MallOrderCreateService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallOrderCreateService(mapper, cartService, memberService, inventoryPort, null, memberMessagePort);
    }

    @Test
    void createsOrderWithSnapshotAndOperationLog()
    {
        when(mapper.selectByIdempotencyKey(1L, "idem-001")).thenReturn(null);
        when(cartService.selectCart(1L)).thenReturn(cart());
        when(memberService.addresses(1L)).thenReturn(List.of(address(7L)));
        when(mapper.insertOrderIgnore(any())).thenAnswer(invocation -> { ((MallOrder) invocation.getArgument(0)).setOrderId(100L); return 1; });
        when(mapper.insertItem(any())).thenReturn(1);
        when(mapper.insertOperationLog(any())).thenReturn(1);
        MallCreateOrderRequest request = new MallCreateOrderRequest(); request.setIdempotencyKey("idem-001"); request.setAddressId(7L);

        MallOrder order = service.create(1L, request);

        assertEquals(100L, order.getOrderId());
        assertEquals("PENDING_PAYMENT", order.getStatus());
        verify(mapper).insertItem(any());
        verify(mapper).insertOperationLog(any());
        verify(memberMessagePort).publish(1L, "ORDER", "订单已创建", "订单 " + order.getOrderNo() + " 已创建，等待支付",
                "请在支付时限内完成付款", "ORDER", 100L, order.getOrderNo(), "/orders/100");
        verify(inventoryPort).reserve(order.getOrderNo(), 10L, 2);
    }

    @Test
    void repeatedIdempotencyKeyReturnsExistingOrderWithoutWriting()
    {
        MallOrder existing = new MallOrder(); existing.setOrderId(88L); existing.setOrderNo("M-EXISTING");
        when(mapper.selectByIdempotencyKey(1L, "idem-001")).thenReturn(existing);
        MallCreateOrderRequest request = new MallCreateOrderRequest(); request.setIdempotencyKey("idem-001");

        MallOrder order = service.create(1L, request);

        assertEquals("M-EXISTING", order.getOrderNo());
        verify(mapper, never()).insertOrderIgnore(any());
        verify(cartService, never()).selectCart(1L);
        verify(inventoryPort, never()).reserve(any(), any(), any(Integer.class));
    }

    @Test
    void cannotCreateWhenCartCannotCheckout()
    {
        when(mapper.selectByIdempotencyKey(1L, "idem-001")).thenReturn(null);
        MallCartResult cart = new MallCartResult(); cart.setItems(List.of()); cart.setCanCheckout(false);
        when(cartService.selectCart(1L)).thenReturn(cart);
        MallCreateOrderRequest request = new MallCreateOrderRequest(); request.setIdempotencyKey("idem-001");

        assertThrows(ServiceException.class, () -> service.create(1L, request));
        verify(mapper, never()).insertOrderIgnore(any());
    }

    @Test
    void reservationFailureAbortsOrderCreation()
    {
        when(mapper.selectByIdempotencyKey(1L, "idem-001")).thenReturn(null);
        when(cartService.selectCart(1L)).thenReturn(cart());
        when(memberService.addresses(1L)).thenReturn(List.of(address(7L)));
        when(mapper.insertOrderIgnore(any())).thenAnswer(invocation -> { ((MallOrder) invocation.getArgument(0)).setOrderId(100L); return 1; });
        when(mapper.insertItem(any())).thenReturn(1);
        doThrow(new ServiceException("库存不足")).when(inventoryPort).reserve(any(), any(), any(Integer.class));
        MallCreateOrderRequest request = new MallCreateOrderRequest(); request.setIdempotencyKey("idem-001"); request.setAddressId(7L);

        assertThrows(ServiceException.class, () -> service.create(1L, request));
        verify(mapper, never()).insertOperationLog(any());
    }

    private MallCartResult cart()
    {
        MallCartItem item = new MallCartItem(); item.setSkuId(10L); item.setSkuCode("SKU-10"); item.setProductName("测试商品");
        item.setSkuName("黑色"); item.setProductImage("/assets/chair.jpg"); item.setPrice(new BigDecimal("12.50"));
        item.setQuantity(2); item.setLineAmount(new BigDecimal("25.00")); item.setSelectedFlag("1"); item.setValid(true); item.setStockShortage(false);
        MallCartResult result = new MallCartResult(); result.setItems(List.of(item)); result.setCanCheckout(true); return result;
    }

    private MallMemberAddress address(Long addressId)
    {
        MallMemberAddress address = new MallMemberAddress(); address.setAddressId(addressId); address.setIsDefault("1");
        address.setReceiverName("张三"); address.setReceiverPhone("13800138000"); address.setProvince("广东省");
        address.setCity("深圳市"); address.setDistrict("南山区"); address.setDetailAddress("科技园 1 号"); return address;
    }
}
