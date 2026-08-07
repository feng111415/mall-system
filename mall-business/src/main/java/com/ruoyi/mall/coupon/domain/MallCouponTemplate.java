package com.ruoyi.mall.coupon.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MallCouponTemplate implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long couponId;
    private String couponName;
    private String scopeType;
    private BigDecimal thresholdAmount;
    private BigDecimal discountAmount;
    private Integer totalQuantity;
    private Integer claimedQuantity;
    private Integer perMemberLimit;
    private String status;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String remark;
    private Integer version;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private List<Long> scopeTargetIds;

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
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer totalQuantity) { this.totalQuantity = totalQuantity; }
    public Integer getClaimedQuantity() { return claimedQuantity; }
    public void setClaimedQuantity(Integer claimedQuantity) { this.claimedQuantity = claimedQuantity; }
    public Integer getPerMemberLimit() { return perMemberLimit; }
    public void setPerMemberLimit(Integer perMemberLimit) { this.perMemberLimit = perMemberLimit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDateTime validFrom) { this.validFrom = validFrom; }
    public LocalDateTime getValidTo() { return validTo; }
    public void setValidTo(LocalDateTime validTo) { this.validTo = validTo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getCreateBy() { return createBy; }
    public void setCreateBy(String createBy) { this.createBy = createBy; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public String getUpdateBy() { return updateBy; }
    public void setUpdateBy(String updateBy) { this.updateBy = updateBy; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<Long> getScopeTargetIds() { return scopeTargetIds; }
    public void setScopeTargetIds(List<Long> scopeTargetIds) { this.scopeTargetIds = scopeTargetIds; }
}
