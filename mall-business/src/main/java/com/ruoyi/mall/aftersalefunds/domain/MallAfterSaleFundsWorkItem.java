package com.ruoyi.mall.aftersalefunds.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MallAfterSaleFundsWorkItem implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String kind;
    private Long recordId;
    private String businessNo;
    private String orderNo;
    private BigDecimal amount;
    private String status;
    private Integer priority;
    private String riskSignal;
    private LocalDateTime updatedAt;

    public String getKind() { return kind; }
    public void setKind(String value) { kind = value; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long value) { recordId = value; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String value) { businessNo = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getPriority() { return priority; }
    public void setPriority(Integer value) { priority = value; }
    public String getRiskSignal() { return riskSignal; }
    public void setRiskSignal(String value) { riskSignal = value; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt = value; }
}
