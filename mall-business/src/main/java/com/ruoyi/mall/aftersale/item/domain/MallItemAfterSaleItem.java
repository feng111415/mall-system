package com.ruoyi.mall.aftersale.item.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallItemAfterSaleItem implements Serializable
{
    private Long afterSaleItemId;
    private Long afterSaleId;
    private Long orderItemId;
    private Long skuId;
    private String productName;
    private String skuName;
    private Integer orderQuantity;
    private Integer requestedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal refundAmount;

    public Long getAfterSaleItemId() { return afterSaleItemId; }
    public void setAfterSaleItemId(Long value) { this.afterSaleItemId = value; }
    public Long getAfterSaleId() { return afterSaleId; }
    public void setAfterSaleId(Long value) { this.afterSaleId = value; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long value) { this.orderItemId = value; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long value) { this.skuId = value; }
    public String getProductName() { return productName; }
    public void setProductName(String value) { this.productName = value; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String value) { this.skuName = value; }
    public Integer getOrderQuantity() { return orderQuantity; }
    public void setOrderQuantity(Integer value) { this.orderQuantity = value; }
    public Integer getRequestedQuantity() { return requestedQuantity; }
    public void setRequestedQuantity(Integer value) { this.requestedQuantity = value; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal value) { this.unitPrice = value; }
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal value) { this.refundAmount = value; }
}
