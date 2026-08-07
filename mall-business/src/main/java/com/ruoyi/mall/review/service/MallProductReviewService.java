package com.ruoyi.mall.review.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.review.domain.MallProductReview;
import com.ruoyi.mall.review.domain.MallProductReviewOrderItem;
import com.ruoyi.mall.review.domain.MallProductReviewProductView;
import com.ruoyi.mall.review.domain.MallProductReviewQuery;
import com.ruoyi.mall.review.domain.MallProductReviewSummary;
import com.ruoyi.mall.review.domain.dto.MallProductReviewAuditRequest;
import com.ruoyi.mall.review.domain.dto.MallProductReviewRequest;
import com.ruoyi.mall.review.mapper.MallProductReviewMapper;

@Service
public class MallProductReviewService
{
    private static final int MAX_IMAGES = 6;
    private static final int MAX_LIMIT = 50;
    private final MallProductReviewMapper mapper;

    public MallProductReviewService(MallProductReviewMapper mapper) { this.mapper = mapper; }

    public MallProductReviewProductView publishedProduct(Long spuId, Integer limit, Integer offset)
    {
        if (spuId == null || spuId <= 0) throw new ServiceException("商品参数无效");
        int safeLimit = limit == null ? 10 : Math.min(Math.max(limit, 1), MAX_LIMIT);
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        MallProductReviewProductView view = new MallProductReviewProductView();
        MallProductReviewSummary summary = mapper.selectPublishedSummary(spuId);
        view.setSummary(summary == null ? new MallProductReviewSummary() : summary);
        List<MallProductReview> reviews = mapper.selectPublishedReviews(spuId, safeLimit, safeOffset);
        if (reviews == null) reviews = Collections.emptyList();
        reviews.forEach(this::attachImages);
        view.setReviews(reviews);
        return view;
    }

    public List<MallProductReviewOrderItem> memberOrderItems(Long memberId, Long orderId)
    {
        requireMember(memberId);
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        List<MallProductReviewOrderItem> values = mapper.selectMemberOrderReviewItems(memberId, orderId);
        return values == null ? Collections.emptyList() : values;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallProductReview submit(Long memberId, MallProductReviewRequest request)
    {
        requireMember(memberId);
        validateRequest(request);
        MallProductReviewOrderItem item = mapper.selectOrderItemForReview(memberId, request.getOrderItemId());
        if (item == null) throw new ServiceException("只有确认收货后的订单商品才能评价");
        if (!item.isCanReview()) throw new ServiceException("该订单商品已经评价过了");
        List<String> images = normalizeImages(request.getImageUrls());

        MallProductReview review = new MallProductReview();
        review.setOrderId(item.getOrderId()); review.setOrderNo(item.getOrderNo());
        review.setOrderItemId(item.getOrderItemId()); review.setMemberId(memberId);
        review.setSpuId(item.getSpuId()); review.setSkuId(item.getSkuId());
        review.setProductName(item.getProductName()); review.setSkuName(item.getSkuName());
        review.setProductImage(item.getProductImage()); review.setRating(request.getRating());
        review.setContent(request.getContent().trim()); review.setAnonymous(Boolean.TRUE.equals(request.getAnonymous()) ? "1" : "0");
        if (mapper.insertReview(review) != 1) throw new ServiceException("评价提交失败，请重试");
        for (int i = 0; i < images.size(); i++) mapper.insertImage(review.getReviewId(), images.get(i), i);
        return detail(review.getReviewId());
    }

    public MallProductReview detail(Long reviewId)
    {
        if (reviewId == null || reviewId <= 0) throw new ServiceException("评价参数无效");
        MallProductReview value = mapper.selectAdminReview(reviewId);
        if (value == null) throw new ServiceException("评价不存在");
        attachImages(value);
        return value;
    }

    public List<MallProductReview> adminList(MallProductReviewQuery query)
    {
        MallProductReviewQuery normalized = query == null ? new MallProductReviewQuery() : query;
        if (StringUtils.isNotBlank(normalized.getKeyword())) normalized.setKeyword(normalized.getKeyword().trim());
        if (normalized.getStatus() != null && !normalized.getStatus().isBlank()
                && !List.of("PENDING", "PUBLISHED", "REJECTED").contains(normalized.getStatus()))
            throw new ServiceException("评价状态无效");
        List<MallProductReview> values = mapper.selectAdminReviews(normalized);
        if (values == null) return Collections.emptyList();
        values.forEach(this::attachImages);
        return values;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallProductReview audit(Long reviewId, MallProductReviewAuditRequest request, String operator)
    {
        if (reviewId == null || reviewId <= 0) throw new ServiceException("评价参数无效");
        if (request == null || !("PUBLISHED".equals(request.getStatus()) || "REJECTED".equals(request.getStatus())))
            throw new ServiceException("审核状态无效");
        String remark = request.getRemark() == null ? null : request.getRemark().trim();
        if ("REJECTED".equals(request.getStatus()) && StringUtils.isBlank(remark))
            throw new ServiceException("驳回评价必须填写原因");
        if (remark != null && remark.length() > 500) throw new ServiceException("审核备注不能超过500个字符");
        if (mapper.updateAudit(reviewId, request.getStatus(), operator, remark) != 1)
            throw new ServiceException("评价状态已变化，请刷新后重试");
        return detail(reviewId);
    }

    private void validateRequest(MallProductReviewRequest request)
    {
        if (request == null || request.getOrderItemId() == null || request.getOrderItemId() <= 0)
            throw new ServiceException("评价订单项无效");
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5)
            throw new ServiceException("评分必须为1至5星");
        if (StringUtils.isBlank(request.getContent())) throw new ServiceException("评价内容不能为空");
        if (request.getContent().trim().length() > 1000) throw new ServiceException("评价内容不能超过1000个字符");
    }

    private List<String> normalizeImages(List<String> values)
    {
        if (values == null || values.isEmpty()) return Collections.emptyList();
        if (values.size() > MAX_IMAGES) throw new ServiceException("评价图片最多上传6张");
        List<String> result = new ArrayList<>();
        for (String value : values)
        {
            if (StringUtils.isBlank(value)) continue;
            String url = value.trim();
            if (url.length() > 500 || !(url.startsWith("/profile/") || url.startsWith("https://") || url.startsWith("http://")))
                throw new ServiceException("评价图片地址无效");
            if (!result.contains(url)) result.add(url);
        }
        return result;
    }

    private void attachImages(MallProductReview value)
    {
        value.setImageUrls(mapper.selectImageUrls(value.getReviewId()));
    }

    private void requireMember(Long memberId)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
    }
}
