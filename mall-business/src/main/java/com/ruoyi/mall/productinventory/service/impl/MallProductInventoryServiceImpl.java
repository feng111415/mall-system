package com.ruoyi.mall.productinventory.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryDetail;
import com.ruoyi.mall.productinventory.domain.MallProductInventoryQuery;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySku;
import com.ruoyi.mall.productinventory.domain.MallProductInventorySummary;
import com.ruoyi.mall.productinventory.mapper.MallProductInventoryMapper;
import com.ruoyi.mall.productinventory.service.MallProductInventoryService;

@Service
public class MallProductInventoryServiceImpl implements MallProductInventoryService
{
    private final MallProductInventoryMapper mapper;

    public MallProductInventoryServiceImpl(MallProductInventoryMapper mapper) { this.mapper = mapper; }

    @Override
    public List<MallProductInventorySummary> list(MallProductInventoryQuery query)
    {
        normalize(query);
        return mapper.selectSummaryList(query);
    }

    @Override
    public MallProductInventoryDetail detail(Long spuId)
    {
        if (spuId == null || spuId <= 0) throw new ServiceException("商品 ID 无效");
        MallProductInventoryDetail detail = mapper.selectDetail(spuId);
        if (detail == null) return null;
        List<MallProductInventorySku> skuList = mapper.selectSkuInventoryList(spuId);
        detail.setSkuList(skuList);
        summarize(detail, skuList);
        return detail;
    }

    private void normalize(MallProductInventoryQuery query)
    {
        if (query == null) return;
        query.setProductKeyword(trimToNull(query.getProductKeyword()));
        query.setSpuCode(trimToNull(query.getSpuCode()));
        query.setSkuCode(trimToNull(query.getSkuCode()));
        query.setPublishStatus(normalizeStatus(query.getPublishStatus(), "0", "1"));
        query.setStockStatus(normalizeStatus(query.getStockStatus(), "HEALTHY", "LOW_STOCK", "OUT_OF_STOCK"));
    }

    private void summarize(MallProductInventoryDetail detail, List<MallProductInventorySku> skuList)
    {
        int available = 0, locked = 0, sold = 0, low = 0, out = 0;
        for (MallProductInventorySku sku : skuList)
        {
            available += value(sku.getAvailableQuantity());
            locked += value(sku.getLockedQuantity());
            sold += value(sku.getSoldQuantity());
            if ("LOW_STOCK".equals(sku.getStockStatus())) low++;
            if ("OUT_OF_STOCK".equals(sku.getStockStatus())) out++;
        }
        detail.setSkuCount(skuList.size());
        detail.setAvailableQuantity(available);
        detail.setLockedQuantity(locked);
        detail.setSoldQuantity(sold);
        detail.setLowStockSkuCount(low);
        detail.setOutOfStockSkuCount(out);
        detail.setStockStatus(out > 0 ? "OUT_OF_STOCK" : low > 0 ? "LOW_STOCK" : "HEALTHY");
    }

    private static String normalizeStatus(String value, String... accepted)
    {
        String normalized = trimToNull(value);
        if (normalized == null) return null;
        for (String candidate : accepted) if (candidate.equals(normalized)) return normalized;
        return null;
    }

    private static String trimToNull(String value)
    {
        if (value == null) return null;
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static int value(Integer value) { return value == null ? 0 : value; }
}
