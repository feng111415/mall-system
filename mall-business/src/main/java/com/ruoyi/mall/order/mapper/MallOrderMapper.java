package com.ruoyi.mall.order.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

public interface MallOrderMapper
{
    List<MallOrder> selectMemberOrders(@Param("memberId") Long memberId,
            @Param("status") String status, @Param("limit") int limit, @Param("offset") int offset);

    List<MallOrder> selectMemberAfterSaleOrders(@Param("memberId") Long memberId,
            @Param("limit") int limit, @Param("offset") int offset);

    MallOrder selectMemberOrder(@Param("orderId") Long orderId, @Param("memberId") Long memberId);

    List<MallOrderItem> selectMemberOrderItems(@Param("orderId") Long orderId,
            @Param("memberId") Long memberId);

    List<MallOrderOperationLog> selectMemberOrderOperations(@Param("orderId") Long orderId,
            @Param("memberId") Long memberId);

    MallOrder selectByIdForUpdate(@Param("orderId") Long orderId, @Param("memberId") Long memberId);

    List<MallOrder> selectTimeoutCandidates(@Param("unpaidCutoffTime") LocalDateTime unpaidCutoffTime,
            @Param("payingCutoffTime") LocalDateTime payingCutoffTime, @Param("limit") int limit);

    MallOrder selectByIdempotencyKey(@Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    int insertOrderIgnore(MallOrder order);

    int insertItem(MallOrderItem item);

    int insertOperationLog(MallOrderOperationLog log);

    int updateStatus(@Param("orderId") Long orderId, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("cancelReason") String cancelReason);

    int closeExpired(@Param("orderId") Long orderId, @Param("paymentStatus") String paymentStatus,
            @Param("cancelReason") String cancelReason);

    int markPaymentPaying(@Param("orderId") Long orderId);

    int resetPaymentUnpaid(@Param("orderId") Long orderId);

    int markPaymentSuccess(@Param("orderId") Long orderId);

    int markLatePaymentRefunding(@Param("orderId") Long orderId);

    int markLatePaymentRefunded(@Param("orderId") Long orderId);

    int markShipped(@Param("orderId") Long orderId);

    int markCompleted(@Param("orderId") Long orderId);

    int markRefunding(@Param("orderId") Long orderId, @Param("fromStatus") String fromStatus);

    int markRefundSuccess(@Param("orderId") Long orderId);

    int restoreAfterRefundReject(@Param("orderId") Long orderId, @Param("originalStatus") String originalStatus);

    int markRiskPassed(@Param("orderId") Long orderId);
}
