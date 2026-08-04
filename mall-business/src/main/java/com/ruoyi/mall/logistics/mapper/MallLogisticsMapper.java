package com.ruoyi.mall.logistics.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.logistics.domain.MallFulfillmentOrder;
import com.ruoyi.mall.logistics.domain.MallFulfillmentQuery;
import com.ruoyi.mall.logistics.domain.MallFulfillmentSummary;

public interface MallLogisticsMapper
{
    MallLogisticsShipment selectByOrderIdForUpdate(@Param("orderId") Long orderId);

    MallLogisticsShipment selectMemberShipment(@Param("orderId") Long orderId,
            @Param("memberId") Long memberId);

    MallLogisticsShipment selectById(@Param("shipmentId") Long shipmentId);

    MallLogisticsShipment selectByIdForUpdate(@Param("shipmentId") Long shipmentId);

    List<MallLogisticsNode> selectNodes(@Param("shipmentId") Long shipmentId);

    int insertShipment(MallLogisticsShipment shipment);

    int insertNode(MallLogisticsNode node);

    MallLogisticsNode selectLatestNode(@Param("shipmentId") Long shipmentId);

    int updateShipmentDelivered(@Param("shipmentId") Long shipmentId,
            @Param("deliveredTime") java.time.LocalDateTime deliveredTime);

    List<MallLogisticsShipment> selectDeliveredBefore(@Param("cutoffTime") java.time.LocalDateTime cutoffTime);

    List<MallFulfillmentOrder> selectFulfillmentOrders(MallFulfillmentQuery query);

    MallFulfillmentSummary selectFulfillmentSummary(MallFulfillmentQuery query);
}
