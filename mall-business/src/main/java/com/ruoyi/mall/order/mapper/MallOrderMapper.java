package com.ruoyi.mall.order.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

public interface MallOrderMapper
{
    MallOrder selectByIdForUpdate(@Param("orderId") Long orderId, @Param("memberId") Long memberId);

    List<MallOrder> selectTimeoutCandidates(@Param("cutoffTime") LocalDateTime cutoffTime,
            @Param("limit") int limit);

    MallOrder selectByIdempotencyKey(@Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    int insertOrderIgnore(MallOrder order);

    int insertItem(MallOrderItem item);

    int insertOperationLog(MallOrderOperationLog log);

    int updateStatus(@Param("orderId") Long orderId, @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus, @Param("cancelReason") String cancelReason);

    int markPaymentPaying(@Param("orderId") Long orderId);

    int markPaymentSuccess(@Param("orderId") Long orderId);
}
