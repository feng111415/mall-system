package com.ruoyi.mall.inventory.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.inventory.domain.MallStock;
import com.ruoyi.mall.inventory.domain.MallStockLog;
import com.ruoyi.mall.inventory.domain.MallStockReservation;

public interface MallInventoryMapper
{
    int insertAllStocksIfAbsent();
    int insertStockIfAbsent(Long skuId);
    MallStock selectBySkuId(Long skuId);
    MallStock selectBySkuIdForUpdate(Long skuId);
    List<MallStock> selectStockList(MallStock query);
    List<MallStockLog> selectLogList(@Param("skuId") Long skuId, @Param("operationType") String operationType);

    MallStockReservation selectReservationForUpdate(@Param("orderNo") String orderNo, @Param("skuId") Long skuId);
    List<MallStockReservation> selectReservationsForUpdate(String orderNo);
    int insertReservationIgnore(MallStockReservation reservation);
    int updateReservationStatus(@Param("reservationId") Long reservationId, @Param("status") String status);

    int reserveStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
    int releaseStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
    int confirmStock(@Param("skuId") Long skuId, @Param("quantity") int quantity);
    int adjustStock(@Param("skuId") Long skuId, @Param("delta") int delta,
        @Param("warningThreshold") Integer warningThreshold);
    int syncLegacySkuStock(@Param("skuId") Long skuId);
    int insertLog(MallStockLog log);
}
