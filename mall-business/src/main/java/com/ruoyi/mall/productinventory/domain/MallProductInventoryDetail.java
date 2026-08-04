package com.ruoyi.mall.productinventory.domain;

import java.util.List;

public class MallProductInventoryDetail extends MallProductInventorySummary
{
    private static final long serialVersionUID = 1L;
    private List<MallProductInventorySku> skuList;

    public List<MallProductInventorySku> getSkuList() { return skuList; }
    public void setSkuList(List<MallProductInventorySku> value) { skuList = value; }
}
