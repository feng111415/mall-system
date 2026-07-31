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
    private LocalDateTime payTime;
    private LocalDateTime closeTime;
    private LocalDateTime updateTime;
    private List<MallOrderItem> items;
    private List<MallOrderOperationLog> operations;
    private LocalDateTime paymentCreateDeadline;
    private LocalDateTime paymentResultDeadline;
    private Boolean canCreatePayment;
    private Boolean canConfirmPayment;
    private Boolean canCancel;
    private String displayStatus;
    private Integer activeAfterSaleCount;
    private Long latestAfterSaleId;
    private String latestAfterSaleType;
    private String latestAfterSaleStatus;
    private LocalDateTime latestAfterSaleUpdateTime;

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
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime payTime) { this.payTime = payTime; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime closeTime) { this.closeTime = closeTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<MallOrderItem> getItems() { return items; }
    public void setItems(List<MallOrderItem> items) { this.items = items; }
    public List<MallOrderOperationLog> getOperations() { return operations; }
    public void setOperations(List<MallOrderOperationLog> operations) { this.operations = operations; }
    public LocalDateTime getPaymentCreateDeadline() { return paymentCreateDeadline; }
    public void setPaymentCreateDeadline(LocalDateTime value) { this.paymentCreateDeadline = value; }
    public LocalDateTime getPaymentResultDeadline() { return paymentResultDeadline; }
    public void setPaymentResultDeadline(LocalDateTime value) { this.paymentResultDeadline = value; }
    public Boolean getCanCreatePayment() { return canCreatePayment; }
    public void setCanCreatePayment(Boolean canCreatePayment) { this.canCreatePayment = canCreatePayment; }
    public Boolean getCanConfirmPayment() { return canConfirmPayment; }
    public void setCanConfirmPayment(Boolean canConfirmPayment) { this.canConfirmPayment = canConfirmPayment; }
    public Boolean getCanCancel() { return canCancel; }
    public void setCanCancel(Boolean canCancel) { this.canCancel = canCancel; }
    public String getDisplayStatus() { return displayStatus; }
    public void setDisplayStatus(String displayStatus) { this.displayStatus = displayStatus; }
    public Integer getActiveAfterSaleCount() { return activeAfterSaleCount; }
    public void setActiveAfterSaleCount(Integer activeAfterSaleCount) { this.activeAfterSaleCount = activeAfterSaleCount; }
    public Long getLatestAfterSaleId() { return latestAfterSaleId; }
    public void setLatestAfterSaleId(Long latestAfterSaleId) { this.latestAfterSaleId = latestAfterSaleId; }
    public String getLatestAfterSaleType() { return latestAfterSaleType; }
    public void setLatestAfterSaleType(String latestAfterSaleType) { this.latestAfterSaleType = latestAfterSaleType; }
    public String getLatestAfterSaleStatus() { return latestAfterSaleStatus; }
    public void setLatestAfterSaleStatus(String latestAfterSaleStatus) { this.latestAfterSaleStatus = latestAfterSaleStatus; }
    public LocalDateTime getLatestAfterSaleUpdateTime() { return latestAfterSaleUpdateTime; }
    public void setLatestAfterSaleUpdateTime(LocalDateTime latestAfterSaleUpdateTime) { this.latestAfterSaleUpdateTime = latestAfterSaleUpdateTime; }
}
