package com.ruoyi.mall.logistics.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;

public interface MallLogisticsMapper
{
    MallLogisticsShipment selectByOrderIdForUpdate(@Param("orderId") Long orderId);

    MallLogisticsShipment selectMemberShipment(@Param("orderId") Long orderId,
            @Param("memberId") Long memberId);

    MallLogisticsShipment selectById(@Param("shipmentId") Long shipmentId);

    List<MallLogisticsNode> selectNodes(@Param("shipmentId") Long shipmentId);

    int insertShipment(MallLogisticsShipment shipment);

    int insertNode(MallLogisticsNode node);
}
