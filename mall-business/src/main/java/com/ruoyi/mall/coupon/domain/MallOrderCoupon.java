package com.ruoyi.mall.coupon.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallOrderCoupon implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long orderCouponId;
    private Long orderId;
    private String orderNo;
    private Long memberCouponId;
    private Long couponId;
    private String couponName;
    private String scopeType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private String status;

    public Long getOrderCouponId() { return orderCouponId; }
    public void setOrderCouponId(Long orderCouponId) { this.orderCouponId = orderCouponId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMemberCouponId() { return memberCouponId; }
    public void setMemberCouponId(Long memberCouponId) { this.memberCouponId = memberCouponId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public BigDecimal getThresholdAmount() { return thresholdAmount; }
    public void setThresholdAmount(BigDecimal thresholdAmount) { this.thresholdAmount = thresholdAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
