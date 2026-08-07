package com.ruoyi.mall.coupon.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.coupon.domain.MallCouponTemplate;
import com.ruoyi.mall.coupon.domain.MallMemberCoupon;
import com.ruoyi.mall.coupon.domain.MallOrderCoupon;

public interface MallCouponMapper
{
    MallMemberCoupon selectMemberCoupon(@Param("memberCouponId") Long memberCouponId,
            @Param("memberId") Long memberId);

    List<Long> selectEligibleSkuIds(@Param("couponId") Long couponId,
            @Param("scopeType") String scopeType, @Param("skuIds") List<Long> skuIds);

    List<MallMemberCoupon> selectMemberCoupons(@Param("memberId") Long memberId);

    List<MallCouponTemplate> selectClaimableTemplates(@Param("memberId") Long memberId);

    MallCouponTemplate selectTemplateForUpdate(@Param("couponId") Long couponId);

    int countMemberClaims(@Param("couponId") Long couponId, @Param("memberId") Long memberId);

    int incrementClaimed(@Param("couponId") Long couponId, @Param("version") Integer version);

    int insertMemberCoupon(MallMemberCoupon coupon);

    int lockMemberCoupon(@Param("memberCouponId") Long memberCouponId,
            @Param("memberId") Long memberId, @Param("orderId") Long orderId);

    int insertOrderCoupon(MallOrderCoupon coupon);

    int consumeByOrderId(@Param("orderId") Long orderId);

    int releaseByOrderId(@Param("orderId") Long orderId);

    MallOrderCoupon selectMemberOrderCoupon(@Param("orderId") Long orderId,
            @Param("memberId") Long memberId);

    List<MallCouponTemplate> selectAdminTemplates(@Param("keyword") String keyword,
            @Param("status") String status);
    MallCouponTemplate selectAdminTemplate(@Param("couponId") Long couponId);
    List<Long> selectScopeTargetIds(@Param("couponId") Long couponId);
    int insertTemplate(MallCouponTemplate template);
    int updateTemplate(MallCouponTemplate template);
    int deleteScopes(@Param("couponId") Long couponId);
    int insertScope(@Param("couponId") Long couponId, @Param("targetId") Long targetId);
    int updateTemplateStatus(@Param("couponId") Long couponId, @Param("status") String status,
            @Param("updateBy") String updateBy);
    int insertAudit(@Param("couponId") Long couponId, @Param("actionType") String actionType,
            @Param("operatorId") String operatorId, @Param("detail") String detail);
}
