package com.ruoyi.mall.order.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MallOrder implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String orderNo;
    private Long memberId;
    private String status;
    private String paymentStatus;
    private String riskStatus;
    private String idempotencyKey;
    private BigDecimal productAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private String receiverName;
    private String receiverPhone;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverDetailAddress;
    private String remark;
    private String cancelReason;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime closeTime;
    private LocalDateTime updateTime;
    private List<MallOrderItem> items;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getRiskStatus() { return riskStatus; }
    public void setRiskStatus(String riskStatus) { this.riskStatus = riskStatus; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public BigDecimal getProductAmount() { return productAmount; }
    public void setProductAmount(BigDecimal productAmount) { this.productAmount = productAmount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public void setPayableAmount(BigDecimal payableAmount) { this.payableAmount = payableAmount; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getReceiverProvince() { return receiverProvince; }
    public void setReceiverProvince(String receiverProvince) { this.receiverProvince = receiverProvince; }
    public String getReceiverCity() { return receiverCity; }
    public void setReceiverCity(String receiverCity) { this.receiverCity = receiverCity; }
    public String getReceiverDistrict() { return receiverDistrict; }
    public void setReceiverDistrict(String receiverDistrict) { this.receiverDistrict = receiverDistrict; }
    public String getReceiverDetailAddress() { return receiverDetailAddress; }
    public void setReceiverDetailAddress(String receiverDetailAddress) { this.receiverDetailAddress = receiverDetailAddress; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getCancelReason() { return cancelReason; }
    public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<MallOrderItem> getItems() { return items; }
    public void setItems(List<MallOrderItem> items) { this.items = items; }
}
