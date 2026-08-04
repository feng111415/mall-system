package com.ruoyi.mall.order.operations.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MallOrderOperationsSummary implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long orderId;
    private String orderNo;
    private Long memberId;
    private String memberNickname;
    private String status;
    private String paymentStatus;
    private String riskStatus;
    private BigDecimal productAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime closeTime;
    private LocalDateTime updateTime;
    private Integer itemCount;
    private Integer totalQuantity;
    private String paymentNo;
    private String paymentMethod;
    private String paymentDetailStatus;
    private Integer reservedQuantity;
    private String inventoryStatus;
    private Long shipmentId;
    private String logisticsStatus;
    private String logisticsCompanyName;
    private String trackingNo;
    private Integer afterSaleCount;
    private Long latestAfterSaleId;
    private String latestAfterSaleNo;
    private String latestAfterSaleType;
    private String latestAfterSaleStatus;
    private BigDecimal latestAfterSaleAmount;

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public String getMemberNickname() { return memberNickname; }
    public void setMemberNickname(String value) { memberNickname = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String value) { paymentStatus = value; }
    public String getRiskStatus() { return riskStatus; }
    public void setRiskStatus(String value) { riskStatus = value; }
    public BigDecimal getProductAmount() { return productAmount; }
    public void setProductAmount(BigDecimal value) { productAmount = value; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal value) { shippingFee = value; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal value) { discountAmount = value; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public void setPayableAmount(BigDecimal value) { payableAmount = value; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { createTime = value; }
    public LocalDateTime getPayTime() { return payTime; }
    public void setPayTime(LocalDateTime value) { payTime = value; }
    public LocalDateTime getCloseTime() { return closeTime; }
    public void setCloseTime(LocalDateTime value) { closeTime = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { updateTime = value; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer value) { itemCount = value; }
    public Integer getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(Integer value) { totalQuantity = value; }
    public String getPaymentNo() { return paymentNo; }
    public void setPaymentNo(String value) { paymentNo = value; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String value) { paymentMethod = value; }
    public String getPaymentDetailStatus() { return paymentDetailStatus; }
    public void setPaymentDetailStatus(String value) { paymentDetailStatus = value; }
    public Integer getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(Integer value) { reservedQuantity = value; }
    public String getInventoryStatus() { return inventoryStatus; }
    public void setInventoryStatus(String value) { inventoryStatus = value; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long value) { shipmentId = value; }
    public String getLogisticsStatus() { return logisticsStatus; }
    public void setLogisticsStatus(String value) { logisticsStatus = value; }
    public String getLogisticsCompanyName() { return logisticsCompanyName; }
    public void setLogisticsCompanyName(String value) { logisticsCompanyName = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { trackingNo = value; }
    public Integer getAfterSaleCount() { return afterSaleCount; }
    public void setAfterSaleCount(Integer value) { afterSaleCount = value; }
    public Long getLatestAfterSaleId() { return latestAfterSaleId; }
    public void setLatestAfterSaleId(Long value) { latestAfterSaleId = value; }
    public String getLatestAfterSaleNo() { return latestAfterSaleNo; }
    public void setLatestAfterSaleNo(String value) { latestAfterSaleNo = value; }
    public String getLatestAfterSaleType() { return latestAfterSaleType; }
    public void setLatestAfterSaleType(String value) { latestAfterSaleType = value; }
    public String getLatestAfterSaleStatus() { return latestAfterSaleStatus; }
    public void setLatestAfterSaleStatus(String value) { latestAfterSaleStatus = value; }
    public BigDecimal getLatestAfterSaleAmount() { return latestAfterSaleAmount; }
    public void setLatestAfterSaleAmount(BigDecimal value) { latestAfterSaleAmount = value; }
}
