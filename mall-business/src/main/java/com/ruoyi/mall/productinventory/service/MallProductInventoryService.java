package com.ruoyi.mall.productinventory.service;

import java.util.List;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryDetail;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryQuery;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySummary;

public interface MallProductInventoryService
{
    List<MallProductInventorySummary> list(MallProductInventoryQuery query);
    MallProductInventoryDetail detail(Long spuId);
}
