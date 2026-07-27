package com.ruoyi.mall.inventory.service;

import java.util.List;
import com.ruoyi.mall.inventory.domain.MallStock;
import com.ruoyi.mall.inventory.domain.MallStockLog;
import com.ruoyi.mall.inventory.domain.dto.MallStockAdjustRequest;

public interface IMallInventoryService
{
    List<MallStock> selectStocks(MallStock query, boolean lowStockOnly);
    MallStock selectStock(Long skuId);
    List<MallStockLog> selectLogs(Long skuId, String operationType);
    void adjust(Long skuId, MallStockAdjustRequest request, String operator);
    void reserve(String orderNo, Long skuId, int quantity);
    void release(String orderNo);
    void confirm(String orderNo);
    void confirm(String orderNo, boolean paymentConfirmed);
}
