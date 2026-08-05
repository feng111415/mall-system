package com.ruoyi.mall.productinventory.mapper;

import java.util.List;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryDetail;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryQuery;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySku;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySummary;

public interface MallProductInventoryMapper
{
    long countLowStockSkus();
    List<MallProductInventorySummary> selectSummaryList(MallProductInventoryQuery query);
    MallProductInventoryDetail selectDetail(Long spuId);
    List<MallProductInventorySku> selectSkuInventoryList(Long spuId);
}
