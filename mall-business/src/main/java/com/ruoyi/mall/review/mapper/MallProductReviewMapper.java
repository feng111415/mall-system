package com.ruoyi.mall.review.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.review.domain.MallProductReview;
import com.ruoyi.mall.review.domain.MallProductReviewOrderItem;
import com.ruoyi.mall.review.domain.MallProductReviewQuery;
import com.ruoyi.mall.review.domain.MallProductReviewSummary;

public interface MallProductReviewMapper
{
    MallProductReviewOrderItem selectOrderItemForReview(@Param("memberId") Long memberId,
            @Param("orderItemId") Long orderItemId);

    List<MallProductReviewOrderItem> selectMemberOrderReviewItems(@Param("memberId") Long memberId,
            @Param("orderId") Long orderId);

    int insertReview(MallProductReview review);
    int insertImage(@Param("reviewId") Long reviewId, @Param("imageUrl") String imageUrl,
            @Param("sortNo") int sortNo);

    MallProductReviewSummary selectPublishedSummary(@Param("spuId") Long spuId);
    List<MallProductReview> selectPublishedReviews(@Param("spuId") Long spuId,
            @Param("limit") int limit, @Param("offset") int offset);
    List<String> selectImageUrls(@Param("reviewId") Long reviewId);

    List<MallProductReview> selectAdminReviews(MallProductReviewQuery query);
    MallProductReview selectAdminReview(@Param("reviewId") Long reviewId);
    int updateAudit(@Param("reviewId") Long reviewId, @Param("status") String status,
            @Param("auditBy") String auditBy, @Param("remark") String remark);
}
