package com.ruoyi.mall.coupon.domain;

import java.math.BigDecimal;

public class MallCouponQuote
{
    private Long memberCouponId;
    private String couponName;
    private BigDecimal eligibleAmount;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;

    public Long getMemberCouponId() { return memberCouponId; }
    public void setMemberCouponId(Long memberCouponId) { this.memberCouponId = memberCouponId; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public BigDecimal getEligibleAmount() { return eligibleAmount; }
    public void setEligibleAmount(BigDecimal eligibleAmount) { this.eligibleAmount = eligibleAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public void setPayableAmount(BigDecimal payableAmount) { this.payableAmount = payableAmount; }
}
