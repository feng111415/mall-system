package com.ruoyi.mall.order.operations.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSaleItem;
import com.ruoyi.mall.inventory.domain.MallStockReservation;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsDetail;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsQuery;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsSummary;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.risk.domain.MallRiskRecord;

public interface MallOrderOperationsMapper
{
    long countOrdersSince(@Param("from") java.time.LocalDateTime from);
    java.math.BigDecimal sumPaidAmountSince(@Param("from") java.time.LocalDateTime from);
    long countOrdersByStatus(@Param("status") String status);
    List<MallOrderOperationsSummary> selectOrders(MallOrderOperationsQuery query);
    MallOrderOperationsDetail selectOrderDetail(@Param("orderId") Long orderId);
    List<MallOrderItem> selectItems(@Param("orderId") Long orderId);
    List<MallPayment> selectPayments(@Param("orderId") Long orderId);
    List<MallStockReservation> selectReservations(@Param("orderNo") String orderNo);
    MallLogisticsShipment selectShipment(@Param("orderId") Long orderId);
    List<MallLogisticsNode> selectShipmentNodes(@Param("shipmentId") Long shipmentId);
    List<MallItemAfterSale> selectAfterSales(@Param("orderId") Long orderId);
    List<MallItemAfterSaleItem> selectAfterSaleItems(@Param("afterSaleId") Long afterSaleId);
    List<MallRiskRecord> selectRiskRecords(@Param("orderId") Long orderId);
    List<MallOrderOperationLog> selectOperations(@Param("orderId") Long orderId);
}
