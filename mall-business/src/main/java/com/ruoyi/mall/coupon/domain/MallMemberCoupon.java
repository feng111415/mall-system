package com.ruoyi.mall.coupon.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MallMemberCoupon implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long memberCouponId;
    private Long couponId;
    private Long memberId;
    private String status;
    private Long lockOrderId;
    private Long usedOrderId;
    private LocalDateTime receiveTime;
    private LocalDateTime lockTime;
    private LocalDateTime usedTime;
    private String couponName;
    private String scopeType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private String templateStatus;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public Long getMemberCouponId() { return memberCouponId; }
    public void setMemberCouponId(Long memberCouponId) { this.memberCouponId = memberCouponId; }
    public Long getCouponId() { return couponId; }
    public void setCouponId(Long couponId) { this.couponId = couponId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getLockOrderId() { return lockOrderId; }
    public void setLockOrderId(Long lockOrderId) { this.lockOrderId = lockOrderId; }
    public Long getUsedOrderId() { return usedOrderId; }
    public void setUsedOrderId(Long usedOrderId) { this.usedOrderId = usedOrderId; }
    public LocalDateTime getReceiveTime() { return receiveTime; }
    public void setReceiveTime(LocalDateTime receiveTime) { this.receiveTime = receiveTime; }
    public LocalDateTime getLockTime() { return lockTime; }
    public void setLockTime(LocalDateTime lockTime) { this.lockTime = lockTime; }
    public LocalDateTime getUsedTime() { return usedTime; }
    public void setUsedTime(LocalDateTime usedTime) { this.usedTime = usedTime; }
    public String getCouponName() { return couponName; }
    public void setCouponName(String couponName) { this.couponName = couponName; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public BigDecimal getThresholdAmount() { return thresholdAmount; }
    public void setThresholdAmount(BigDecimal thresholdAmount) { this.thresholdAmount = thresholdAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public String getTemplateStatus() { return templateStatus; }
    public void setTemplateStatus(String templateStatus) { this.templateStatus = templateStatus; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
}
