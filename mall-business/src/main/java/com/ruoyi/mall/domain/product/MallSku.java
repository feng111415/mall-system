package com.ruoyi.mall.domain.product;

import java.math.BigDecimal;
import com.ruoyi.common.core.domain.BaseEntity;

public class MallSku extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long skuId;
    private Long spuId;
    private String skuCode;
    private String skuName;
    private String specJson;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal marketPrice;
    private Integer availableStock;
    private String status;

    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getSpecJson() { return specJson; }
    public void setSpecJson(String specJson) { this.specJson = specJson; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public BigDecimal getMarketPrice() { return marketPrice; }
    public void setMarketPrice(BigDecimal marketPrice) { this.marketPrice = marketPrice; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
