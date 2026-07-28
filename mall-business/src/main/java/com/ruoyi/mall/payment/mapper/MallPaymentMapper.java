package com.ruoyi.mall.payment.mapper;

import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.payment.domain.MallPayment;

public interface MallPaymentMapper
{
    MallPayment selectByOrderIdempotency(@Param("orderId") Long orderId,
            @Param("memberId") Long memberId, @Param("idempotencyKey") String idempotencyKey);

    MallPayment selectByPaymentNoForUpdate(@Param("paymentNo") String paymentNo);

    MallPayment selectByIdForUpdate(@Param("paymentId") Long paymentId);

    MallPayment selectSuccessByOrderIdForUpdate(@Param("orderId") Long orderId);

    MallPayment selectByPaymentNo(@Param("paymentNo") String paymentNo);

    int insertPayment(MallPayment payment);

    int updateCreationResult(@Param("paymentId") Long paymentId, @Param("status") String status,
            @Param("providerPaymentNo") String providerPaymentNo, @Param("paymentUrl") String paymentUrl,
            @Param("failureReason") String failureReason);

    int updateSuccess(@Param("paymentId") Long paymentId);
}
