package com.ruoyi.mall.productinventory.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MallProductInventorySku implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long skuId;
    private Long spuId;
    private String skuCode;
    private String skuName;
    private String specJson;
    private BigDecimal price;
    private String status;
    private Integer availableQuantity;
    private Integer lockedQuantity;
    private Integer soldQuantity;
    private Integer warningThreshold;
    private String stockStatus;
    private LocalDateTime updateTime;

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long value) { skuId = value; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long value) { spuId = value; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String value) { skuCode = value; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String value) { skuName = value; }
    public String getSpecJson() { return specJson; }
    public void setSpecJson(String value) { specJson = value; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer value) { availableQuantity = value; }
    public Integer getLockedQuantity() { return lockedQuantity; }
    public void setLockedQuantity(Integer value) { lockedQuantity = value; }
    public Integer getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(Integer value) { soldQuantity = value; }
    public Integer getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(Integer value) { warningThreshold = value; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String value) { stockStatus = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { updateTime = value; }
}
