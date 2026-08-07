package com.ruoyi.mall.order.domain.dto;

public class MallCreateOrderRequest
{
    private String idempotencyKey;
    private Long addressId;
    private String remark;
    private Long memberCouponId;

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Long getMemberCouponId() { return memberCouponId; }
    public void setMemberCouponId(Long memberCouponId) { this.memberCouponId = memberCouponId; }
}
