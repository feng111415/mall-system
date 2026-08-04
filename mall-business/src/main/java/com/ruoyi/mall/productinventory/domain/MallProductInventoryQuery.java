package com.ruoyi.mall.productinventory.domain;

import java.io.Serializable;

public class MallProductInventoryQuery implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String productKeyword;
    private String spuCode;
    private String skuCode;
    private String publishStatus;
    private String stockStatus;

    public String getProductKeyword() { return productKeyword; }
    public void setProductKeyword(String value) { productKeyword = value; }
    public String getSpuCode() { return spuCode; }
    public void setSpuCode(String value) { spuCode = value; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String value) { skuCode = value; }
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String value) { publishStatus = value; }
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String value) { stockStatus = value; }
}
