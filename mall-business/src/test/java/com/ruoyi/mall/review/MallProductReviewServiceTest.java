package com.ruoyi.mall.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.review.domain.MallProductReview;
import com.ruoyi.mall.review.domain.MallProductReviewOrderItem;
import com.ruoyi.mall.review.domain.MallProductReviewSummary;
import com.ruoyi.mall.review.domain.dto.MallProductReviewAuditRequest;
import com.ruoyi.mall.review.domain.dto.MallProductReviewRequest;
import com.ruoyi.mall.review.mapper.MallProductReviewMapper;
import com.ruoyi.mall.review.service.MallProductReviewService;

class MallProductReviewServiceTest
{
    @Mock private MallProductReviewMapper mapper;
    private MallProductReviewService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallProductReviewService(mapper);
    }

    @Test
    void memberCanSubmitOneReviewForCompletedOrderItemWithSixImages()
    {
        MallProductReviewOrderItem item = item();
        when(mapper.selectOrderItemForReview(7L, 101L)).thenReturn(item);
        when(mapper.insertReview(any())).thenAnswer(invocation -> {
            MallProductReview value = invocation.getArgument(0); value.setReviewId(55L); return 1;
        });
        MallProductReview saved = new MallProductReview(); saved.setReviewId(55L);
        when(mapper.selectAdminReview(55L)).thenReturn(saved);
        when(mapper.selectImageUrls(55L)).thenReturn(List.of("/profile/a.jpg"));

        MallProductReviewRequest request = request();
        request.setImageUrls(List.of("/profile/a.jpg", "/profile/b.jpg", "/profile/c.jpg",
                "/profile/d.jpg", "/profile/e.jpg", "/profile/f.jpg"));
        MallProductReview result = service.submit(7L, request);

        assertEquals(55L, result.getReviewId());
        verify(mapper).insertImage(55L, "/profile/f.jpg", 5);
    }

    @Test
    void reviewRequiresCompletedOrderItemAndCannotBeDuplicated()
    {
        MallProductReviewOrderItem missing = null;
        when(mapper.selectOrderItemForReview(7L, 101L)).thenReturn(missing);
        assertThrows(ServiceException.class, () -> service.submit(7L, request()));

        MallProductReviewOrderItem reviewed = item(); reviewed.setCanReview(false);
        when(mapper.selectOrderItemForReview(7L, 101L)).thenReturn(reviewed);
        assertThrows(ServiceException.class, () -> service.submit(7L, request()));
    }

    @Test
    void reviewRejectsMoreThanSixImagesAndInvalidRating()
    {
        MallProductReviewRequest tooManyImages = request();
        tooManyImages.setImageUrls(List.of("/1", "/2", "/3", "/4", "/5", "/6", "/7"));
        assertThrows(ServiceException.class, () -> service.submit(7L, tooManyImages));
        MallProductReviewRequest invalidRating = request(); invalidRating.setRating(6);
        assertThrows(ServiceException.class, () -> service.submit(7L, invalidRating));
    }

    @Test
    void pendingReviewCanBePublishedOrRejectedWithReason()
    {
        when(mapper.updateAudit(55L, "PUBLISHED", "ops", null)).thenReturn(1);
        when(mapper.selectAdminReview(55L)).thenReturn(new MallProductReview());
        when(mapper.selectImageUrls(55L)).thenReturn(List.of());
        MallProductReviewAuditRequest publish = new MallProductReviewAuditRequest(); publish.setStatus("PUBLISHED");
        service.audit(55L, publish, "ops");
        verify(mapper).updateAudit(55L, "PUBLISHED", "ops", null);

        MallProductReviewAuditRequest reject = new MallProductReviewAuditRequest(); reject.setStatus("REJECTED");
        assertThrows(ServiceException.class, () -> service.audit(55L, reject, "ops"));
    }

    @Test
    void publishedViewIncludesSummaryAndImages()
    {
        MallProductReviewSummary summary = new MallProductReviewSummary(); summary.setReviewCount(2);
        MallProductReview review = new MallProductReview(); review.setReviewId(88L);
        when(mapper.selectPublishedSummary(3L)).thenReturn(summary);
        when(mapper.selectPublishedReviews(3L, 10, 0)).thenReturn(List.of(review));
        when(mapper.selectImageUrls(88L)).thenReturn(List.of("/profile/review.jpg"));

        var view = service.publishedProduct(3L, null, null);
        assertEquals(2, view.getSummary().getReviewCount());
        assertEquals(List.of("/profile/review.jpg"), view.getReviews().get(0).getImageUrls());
    }

    private MallProductReviewRequest request()
    {
        MallProductReviewRequest value = new MallProductReviewRequest();
        value.setOrderItemId(101L); value.setRating(5); value.setContent("很好用"); value.setAnonymous(true);
        return value;
    }

    private MallProductReviewOrderItem item()
    {
        MallProductReviewOrderItem value = new MallProductReviewOrderItem();
        value.setOrderItemId(101L); value.setOrderId(11L); value.setOrderNo("M11"); value.setMemberId(7L);
        value.setSpuId(3L); value.setSkuId(31L); value.setProductName("商品"); value.setSkuName("标准");
        value.setCanReview(true); return value;
    }
}
