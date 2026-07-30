package com.ruoyi.mall.aftersale.item.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSaleItem;

public interface MallItemAfterSaleMapper
{
    MallItemAfterSale selectById(@Param("afterSaleId") Long afterSaleId);
    MallItemAfterSale selectByIdForUpdate(@Param("afterSaleId") Long afterSaleId);
    MallItemAfterSale selectMemberById(@Param("afterSaleId") Long afterSaleId, @Param("memberId") Long memberId);
    MallItemAfterSale selectMemberByIdForUpdate(@Param("afterSaleId") Long afterSaleId, @Param("memberId") Long memberId);
    List<MallItemAfterSale> selectMemberList(@Param("memberId") Long memberId);
    List<MallItemAfterSaleItem> selectItems(@Param("afterSaleId") Long afterSaleId);
    Integer selectActiveRequestedQuantity(@Param("orderItemId") Long orderItemId);
    int insert(MallItemAfterSale afterSale);
    int insertItem(MallItemAfterSaleItem item);
    int markApproved(@Param("afterSaleId") Long afterSaleId);
    int markRejected(@Param("afterSaleId") Long afterSaleId, @Param("reason") String reason);
    int markReturnTracking(@Param("afterSaleId") Long afterSaleId, @Param("companyCode") String companyCode, @Param("trackingNo") String trackingNo);
    int markRefunding(@Param("afterSaleId") Long afterSaleId);
    int markSuccess(@Param("afterSaleId") Long afterSaleId, @Param("providerRefundNo") String providerRefundNo);
    int markFailed(@Param("afterSaleId") Long afterSaleId, @Param("reason") String reason);
    List<MallItemAfterSale> selectAdminList(@Param("status") String status);
}
