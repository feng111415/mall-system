package com.ruoyi.mall.order.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

public interface MallOrderMapper
{
    MallOrder selectByIdempotencyKey(@Param("memberId") Long memberId,
            @Param("idempotencyKey") String idempotencyKey);

    int insertOrderIgnore(MallOrder order);

    int insertItem(MallOrderItem item);

    int insertOperationLog(MallOrderOperationLog log);
}
