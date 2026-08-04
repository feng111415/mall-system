package com.ruoyi.mall.productinventory.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MallProductInventorySummary implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long spuId;
    private String spuCode;
    private String productName;
    private String subtitle;
    private String mainImage;
    private String categoryName;
    private String brandName;
    private String publishStatus;
    private String stockStatus;
    private Integer skuCount;
    private Integer availableQuantity;
    private Integer lockedQuantity;
    private Integer soldQuantity;
    private Integer lowStockSkuCount;
    private Integer outOfStockSkuCount;
    private LocalDateTime updateTime;

    public Long getSpuId() { return spuId; }
    public void setSpuId(Long value) { spuId = value; }
    public String getSpuCode() { return spuCode; }
    public void setSpuCode(String value) { spuCode = value; }
    public String getProductName() { return productName; }
    public void setProductName(String value) { productName = value; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String value) { subtitle = value; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String value) { mainImage = value; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String value) { categoryName = value; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String value) { brandName = value; }
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String value) { publishStatus = value; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String value) { stockStatus = value; }
    public Integer getSkuCount() { return skuCount; }
    public void setSkuCount(Integer value) { skuCount = value; }
    public Integer getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(Integer value) { availableQuantity = value; }
    public Integer getLockedQuantity() { return lockedQuantity; }
    public void setLockedQuantity(Integer value) { lockedQuantity = value; }
    public Integer getSoldQuantity() { return soldQuantity; }
    public void setSoldQuantity(Integer value) { soldQuantity = value; }
    public Integer getLowStockSkuCount() { return lowStockSkuCount; }
    public void setLowStockSkuCount(Integer value) { lowStockSkuCount = value; }
    public Integer getOutOfStockSkuCount() { return outOfStockSkuCount; }
    public void setOutOfStockSkuCount(Integer value) { outOfStockSkuCount = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { updateTime = value; }
}
