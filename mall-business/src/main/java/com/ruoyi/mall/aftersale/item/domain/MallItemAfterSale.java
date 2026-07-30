package com.ruoyi.mall.aftersale.item.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class MallItemAfterSale implements Serializable
{
    private Long afterSaleId;
    private String afterSaleNo;
    private Long orderId;
    private String orderNo;
    private Long memberId;
    private String type;
    private String reasonCode;
    private String reason;
    private String evidenceUrl;
    private String status;
    private BigDecimal refundAmount;
    private BigDecimal shippingRefundAmount;
    private String returnCompanyCode;
    private String returnTrackingNo;
    private String riskSignal;
    private String failureReason;
    private String providerRefundNo;
    private LocalDateTime deadlineTime;
    private LocalDateTime refundTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MallItemAfterSaleItem> items;

    public Long getAfterSaleId() { return afterSaleId; }
    public void setAfterSaleId(Long value) { this.afterSaleId = value; }
    public String getAfterSaleNo() { return afterSaleNo; }
    public void setAfterSaleNo(String value) { this.afterSaleNo = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { this.orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { this.orderNo = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { this.memberId = value; }
    public String getType() { return type; }
    public void setType(String value) { this.type = value; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String value) { this.reasonCode = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { this.reason = value; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String value) { this.evidenceUrl = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal value) { this.refundAmount = value; }
    public BigDecimal getShippingRefundAmount() { return shippingRefundAmount; }
    public void setShippingRefundAmount(BigDecimal value) { this.shippingRefundAmount = value; }
    public String getReturnCompanyCode() { return returnCompanyCode; }
    public void setReturnCompanyCode(String value) { this.returnCompanyCode = value; }
    public String getReturnTrackingNo() { return returnTrackingNo; }
    public void setReturnTrackingNo(String value) { this.returnTrackingNo = value; }
    public String getRiskSignal() { return riskSignal; }
    public void setRiskSignal(String value) { this.riskSignal = value; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String value) { this.failureReason = value; }
    public String getProviderRefundNo() { return providerRefundNo; }
    public void setProviderRefundNo(String value) { this.providerRefundNo = value; }
    public LocalDateTime getDeadlineTime() { return deadlineTime; }
    public void setDeadlineTime(LocalDateTime value) { this.deadlineTime = value; }
    public LocalDateTime getRefundTime() { return refundTime; }
    public void setRefundTime(LocalDateTime value) { this.refundTime = value; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { this.createTime = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { this.updateTime = value; }
    public List<MallItemAfterSaleItem> getItems() { return items; }
    public void setItems(List<MallItemAfterSaleItem> value) { this.items = value; }
}
