package com.ruoyi.mall.analytics.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.analytics.domain.MallAnalyticsRanking;
import com.ruoyi.mall.analytics.domain.MallAnalyticsTrendPoint;

public interface MallBusinessAnalyticsMapper
{
    long countOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    BigDecimal sumSales(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    long countPaidMembers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    long countRepeatMembers(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    long countPendingShipment();
    long countInTransitShipments();
    long countDeliveredShipments(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    long countLowStockSkus();
    long sumAvailableStock();
    long countAfterSalePending();
    long countAfterSales(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    BigDecimal sumRefunds(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    long countOpenFinancialAnomalies();
    List<MallAnalyticsTrendPoint> selectSalesTrend(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    List<MallAnalyticsRanking> selectProductRanking(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, @Param("limit") int limit);
}
