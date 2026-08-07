package com.ruoyi.mall.coupon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.coupon.domain.MallCouponTemplate;
import com.ruoyi.mall.coupon.domain.MallMemberCoupon;
import com.ruoyi.mall.coupon.mapper.MallCouponMapper;
import com.ruoyi.mall.coupon.service.MallCouponService;

class MallCouponPricingServiceTest
{
    @Mock private MallCouponMapper mapper;
    private MallCouponService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallCouponService(mapper);
    }

    @Test
    void validFixedAmountCouponReducesServerCalculatedPayableAmount()
    {
        MallMemberCoupon coupon = coupon("ALL", "100.00", "20.00");
        when(mapper.selectMemberCoupon(9L, 1L)).thenReturn(coupon);

        var quote = service.quote(1L, 9L, List.of(item(10L, "120.00")));

        assertEquals(new BigDecimal("20.00"), quote.getDiscountAmount());
        assertEquals(new BigDecimal("100.00"), quote.getPayableAmount());
    }

    @Test
    void scopedCouponCountsOnlyEligibleProductsTowardThreshold()
    {
        MallMemberCoupon coupon = coupon("CATEGORY", "100.00", "20.00");
        when(mapper.selectMemberCoupon(9L, 1L)).thenReturn(coupon);
        when(mapper.selectEligibleSkuIds(3L, "CATEGORY", List.of(10L, 11L))).thenReturn(List.of(10L));

        assertThrows(ServiceException.class, () -> service.quote(1L, 9L,
                List.of(item(10L, "80.00"), item(11L, "60.00"))));
    }

    @Test
    void expiredCouponCannotBeQuoted()
    {
        MallMemberCoupon coupon = coupon("ALL", "0.00", "20.00");
        coupon.setValidTo(LocalDateTime.now().minusSeconds(1));
        when(mapper.selectMemberCoupon(9L, 1L)).thenReturn(coupon);

        assertThrows(ServiceException.class,
                () -> service.quote(1L, 9L, List.of(item(10L, "120.00"))));
    }

    @Test
    void memberCanClaimTemplateOnlyOnce()
    {
        MallCouponTemplate template = new MallCouponTemplate(); template.setCouponId(3L);
        template.setCouponName("新人券"); template.setStatus("PUBLISHED");
        template.setTotalQuantity(10); template.setClaimedQuantity(2); template.setPerMemberLimit(1);
        template.setValidTo(LocalDateTime.now().plusDays(1));
        when(mapper.selectTemplateForUpdate(3L)).thenReturn(template);
        when(mapper.countMemberClaims(3L, 1L)).thenReturn(1);

        assertThrows(ServiceException.class, () -> service.claim(1L, 3L));
    }

    @Test
    void couponLockIsAtomicAndPersistsOrderSnapshot()
    {
        MallMemberCoupon coupon = coupon("ALL", "100.00", "20.00");
        when(mapper.selectMemberCoupon(9L, 1L)).thenReturn(coupon);
        when(mapper.lockMemberCoupon(9L, 1L, 88L)).thenReturn(1);
        when(mapper.insertOrderCoupon(any())).thenReturn(1);

        service.lockToOrder(1L, 9L, 88L, "M88", List.of(item(10L, "120.00")));

        verify(mapper).lockMemberCoupon(9L, 1L, 88L);
        verify(mapper).insertOrderCoupon(any());
    }

    @Test
    void alreadyLockedCouponCannotBeUsedByAnotherOrder()
    {
        MallMemberCoupon coupon = coupon("ALL", "100.00", "20.00");
        when(mapper.selectMemberCoupon(9L, 1L)).thenReturn(coupon);
        when(mapper.lockMemberCoupon(9L, 1L, 89L)).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.lockToOrder(
                1L, 9L, 89L, "M89", List.of(item(10L, "120.00"))));
    }

    @Test
    void paymentConsumesCouponAndOrderCloseReleasesIt()
    {
        when(mapper.consumeByOrderId(88L)).thenReturn(1);
        when(mapper.releaseByOrderId(89L)).thenReturn(1);

        service.consume(88L);
        service.release(89L);

        verify(mapper).consumeByOrderId(88L);
        verify(mapper).releaseByOrderId(89L);
    }

    private MallMemberCoupon coupon(String scopeType, String threshold, String discount)
    {
        MallMemberCoupon coupon = new MallMemberCoupon();
        coupon.setMemberCouponId(9L); coupon.setCouponId(3L); coupon.setMemberId(1L);
        coupon.setCouponName("满100减20"); coupon.setScopeType(scopeType);
        coupon.setThresholdAmount(new BigDecimal(threshold));
        coupon.setDiscountAmount(new BigDecimal(discount)); coupon.setStatus("AVAILABLE");
        coupon.setTemplateStatus("PUBLISHED");
        coupon.setValidFrom(LocalDateTime.now().minusDays(1));
        coupon.setValidTo(LocalDateTime.now().plusDays(1));
        return coupon;
    }

    private MallCartItem item(Long skuId, String amount)
    {
        MallCartItem item = new MallCartItem(); item.setSkuId(skuId);
        item.setLineAmount(new BigDecimal(amount)); return item;
    }
}
