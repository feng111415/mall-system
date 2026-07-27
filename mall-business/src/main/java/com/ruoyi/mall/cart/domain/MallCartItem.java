package com.ruoyi.mall.cart.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallCartItem implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long cartItemId;
    private Long memberId;
    private Long skuId;
    private Integer quantity;
    private String selectedFlag;
    private String skuCode;
    private String skuName;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer availableStock;
    private String skuStatus;
    private String skuDelFlag;
    private String spuPublishStatus;
    private String spuDelFlag;
    private Boolean valid;
    private Boolean stockShortage;
    private BigDecimal lineAmount;

    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getSelectedFlag() { return selectedFlag; }
    public void setSelectedFlag(String selectedFlag) { this.selectedFlag = selectedFlag; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String skuName) { this.skuName = skuName; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String productImage) { this.productImage = productImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getAvailableStock() { return availableStock; }
    public void setAvailableStock(Integer availableStock) { this.availableStock = availableStock; }
    public String getSkuStatus() { return skuStatus; }
    public void setSkuStatus(String skuStatus) { this.skuStatus = skuStatus; }
    public String getSkuDelFlag() { return skuDelFlag; }
    public void setSkuDelFlag(String skuDelFlag) { this.skuDelFlag = skuDelFlag; }
    public String getSpuPublishStatus() { return spuPublishStatus; }
    public void setSpuPublishStatus(String spuPublishStatus) { this.spuPublishStatus = spuPublishStatus; }
    public String getSpuDelFlag() { return spuDelFlag; }
    public void setSpuDelFlag(String spuDelFlag) { this.spuDelFlag = spuDelFlag; }
    public Boolean getValid() { return valid; }
    public void setValid(Boolean valid) { this.valid = valid; }
    public Boolean getStockShortage() { return stockShortage; }
    public void setStockShortage(Boolean stockShortage) { this.stockShortage = stockShortage; }
    public BigDecimal getLineAmount() { return lineAmount; }
    public void setLineAmount(BigDecimal lineAmount) { this.lineAmount = lineAmount; }
}
