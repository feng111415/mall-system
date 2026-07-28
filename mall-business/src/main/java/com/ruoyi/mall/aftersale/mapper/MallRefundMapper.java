package com.ruoyi.mall.aftersale.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.aftersale.domain.MallRefund;

public interface MallRefundMapper
{
    MallRefund selectByOrderIdForUpdate(@Param("orderId") Long orderId);
    MallRefund selectByIdForUpdate(@Param("refundId") Long refundId);
    MallRefund selectByRefundNoForUpdate(@Param("refundNo") String refundNo);
    MallRefund selectMemberById(@Param("refundId") Long refundId, @Param("memberId") Long memberId);
    List<MallRefund> selectMemberList(@Param("memberId") Long memberId);
    int insert(MallRefund refund);
    int markRefunding(@Param("refundId") Long refundId);
    int markSuccess(@Param("refundId") Long refundId, @Param("providerRefundNo") String providerRefundNo);
    int markFailed(@Param("refundId") Long refundId, @Param("failureReason") String failureReason);
    int markRejected(@Param("refundId") Long refundId, @Param("failureReason") String failureReason);
}
