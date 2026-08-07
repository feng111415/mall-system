package com.ruoyi.mall.coupon.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.cart.domain.MallCartItem;
import com.ruoyi.mall.coupon.domain.MallCouponQuote;
import com.ruoyi.mall.coupon.domain.MallCouponTemplate;
import com.ruoyi.mall.coupon.domain.MallMemberCoupon;
import com.ruoyi.mall.coupon.domain.MallOrderCoupon;
import com.ruoyi.mall.coupon.mapper.MallCouponMapper;

@Service
public class MallCouponService
{
    private final MallCouponMapper mapper;

    public MallCouponService(MallCouponMapper mapper)
    {
        this.mapper = mapper;
    }

    public MallCouponQuote quote(Long memberId, Long memberCouponId, List<MallCartItem> items)
    {
        BigDecimal productAmount = total(items);
        MallCouponQuote quote = new MallCouponQuote();
        quote.setDiscountAmount(BigDecimal.ZERO); quote.setEligibleAmount(productAmount);
        quote.setPayableAmount(productAmount);
        if (memberCouponId == null) return quote;

        MallMemberCoupon coupon = mapper.selectMemberCoupon(memberCouponId, memberId);
        validateCoupon(coupon);
        BigDecimal eligibleAmount = eligibleAmount(coupon, items);
        if (eligibleAmount.compareTo(zero(coupon.getThresholdAmount())) < 0)
            throw new ServiceException("未达到优惠券使用门槛");
        BigDecimal discount = zero(coupon.getDiscountAmount()).min(eligibleAmount);
        quote.setMemberCouponId(memberCouponId); quote.setCouponName(coupon.getCouponName());
        quote.setEligibleAmount(eligibleAmount); quote.setDiscountAmount(discount);
        quote.setPayableAmount(productAmount.subtract(discount));
        return quote;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallMemberCoupon claim(Long memberId, Long couponId)
    {
        if (memberId == null || memberId <= 0 || couponId == null || couponId <= 0)
            throw new ServiceException("优惠券领取参数无效");
        MallCouponTemplate template = mapper.selectTemplateForUpdate(couponId);
        if (template == null || !"PUBLISHED".equals(template.getStatus()))
            throw new ServiceException("优惠券暂不可领取");
        if (template.getValidTo() == null || LocalDateTime.now().isAfter(template.getValidTo()))
            throw new ServiceException("优惠券已过期");
        int limit = template.getPerMemberLimit() == null ? 1 : template.getPerMemberLimit();
        if (mapper.countMemberClaims(couponId, memberId) >= limit)
            throw new ServiceException("每位会员限领一次");
        if (template.getTotalQuantity() == null || template.getClaimedQuantity() == null
                || template.getClaimedQuantity() >= template.getTotalQuantity()
                || mapper.incrementClaimed(couponId, template.getVersion()) != 1)
            throw new ServiceException("优惠券已领完");
        MallMemberCoupon coupon = new MallMemberCoupon();
        coupon.setCouponId(couponId); coupon.setMemberId(memberId); coupon.setStatus("AVAILABLE");
        coupon.setReceiveTime(LocalDateTime.now());
        if (mapper.insertMemberCoupon(coupon) != 1) throw new ServiceException("优惠券领取失败，请重试");
        return coupon;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCouponQuote lockToOrder(Long memberId, Long memberCouponId, Long orderId,
            String orderNo, List<MallCartItem> items)
    {
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        MallCouponQuote quote = quote(memberId, memberCouponId, items);
        MallMemberCoupon coupon = mapper.selectMemberCoupon(memberCouponId, memberId);
        if (mapper.lockMemberCoupon(memberCouponId, memberId, orderId) != 1)
            throw new ServiceException("优惠券已被其他订单占用，请重新选择");
        MallOrderCoupon snapshot = new MallOrderCoupon();
        snapshot.setOrderId(orderId); snapshot.setOrderNo(orderNo);
        snapshot.setMemberCouponId(memberCouponId); snapshot.setCouponId(coupon.getCouponId());
        snapshot.setCouponName(coupon.getCouponName()); snapshot.setScopeType(coupon.getScopeType());
        snapshot.setThresholdAmount(coupon.getThresholdAmount());
        snapshot.setDiscountAmount(quote.getDiscountAmount()); snapshot.setStatus("LOCKED");
        if (mapper.insertOrderCoupon(snapshot) != 1) throw new ServiceException("优惠券订单快照保存失败");
        return quote;
    }

    @Transactional(rollbackFor = Exception.class)
    public void consume(Long orderId)
    {
        if (orderId != null) mapper.consumeByOrderId(orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void release(Long orderId)
    {
        if (orderId != null) mapper.releaseByOrderId(orderId);
    }

    public List<MallMemberCoupon> memberCoupons(Long memberId)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
        return mapper.selectMemberCoupons(memberId);
    }

    public List<MallCouponTemplate> claimable(Long memberId)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
        return mapper.selectClaimableTemplates(memberId);
    }

    public MallOrderCoupon orderCoupon(Long memberId, Long orderId)
    {
        return mapper.selectMemberOrderCoupon(orderId, memberId);
    }

    private BigDecimal eligibleAmount(MallMemberCoupon coupon, List<MallCartItem> items)
    {
        if ("ALL".equals(coupon.getScopeType())) return total(items);
        List<Long> skuIds = items == null ? List.of() : items.stream().map(MallCartItem::getSkuId)
                .filter(id -> id != null && id > 0).distinct().toList();
        if (skuIds.isEmpty()) return BigDecimal.ZERO;
        Set<Long> eligibleSkuIds = new HashSet<>(mapper.selectEligibleSkuIds(
                coupon.getCouponId(), coupon.getScopeType(), skuIds));
        return items.stream().filter(item -> eligibleSkuIds.contains(item.getSkuId()))
                .map(item -> zero(item.getLineAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateCoupon(MallMemberCoupon coupon)
    {
        if (coupon == null) throw new ServiceException("优惠券不存在或不属于当前会员");
        if (!"AVAILABLE".equals(coupon.getStatus())) throw new ServiceException("优惠券当前不可使用");
        if (!"PUBLISHED".equals(coupon.getTemplateStatus())) throw new ServiceException("优惠券已暂停使用");
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() == null || coupon.getValidTo() == null
                || now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo()))
            throw new ServiceException("优惠券不在有效期内");
    }

    private BigDecimal total(List<MallCartItem> items)
    {
        if (items == null) return BigDecimal.ZERO;
        return items.stream().map(item -> zero(item.getLineAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal zero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }
}
