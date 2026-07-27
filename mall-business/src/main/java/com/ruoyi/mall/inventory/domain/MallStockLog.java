package com.ruoyi.mall.inventory.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Append-only stock change record. */
public class MallStockLog implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long logId;
    private Long skuId;
    private String skuCode;
    private String operationType;
    private String sourceType;
    private String sourceNo;
    private Integer availableChange;
    private Integer lockedChange;
    private Integer soldChange;
    private Integer availableAfter;
    private Integer lockedAfter;
    private Integer soldAfter;
    private String reason;
    private String operator;
    private LocalDateTime createTime;

    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceNo() { return sourceNo; }
    public void setSourceNo(String sourceNo) { this.sourceNo = sourceNo; }
    public Integer getAvailableChange() { return availableChange; }
    public void setAvailableChange(Integer availableChange) { this.availableChange = availableChange; }
    public Integer getLockedChange() { return lockedChange; }
    public void setLockedChange(Integer lockedChange) { this.lockedChange = lockedChange; }
    public Integer getSoldChange() { return soldChange; }
    public void setSoldChange(Integer soldChange) { this.soldChange = soldChange; }
    public Integer getAvailableAfter() { return availableAfter; }
    public void setAvailableAfter(Integer availableAfter) { this.availableAfter = availableAfter; }
    public Integer getLockedAfter() { return lockedAfter; }
    public void setLockedAfter(Integer lockedAfter) { this.lockedAfter = lockedAfter; }
    public Integer getSoldAfter() { return soldAfter; }
    public void setSoldAfter(Integer soldAfter) { this.soldAfter = soldAfter; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
