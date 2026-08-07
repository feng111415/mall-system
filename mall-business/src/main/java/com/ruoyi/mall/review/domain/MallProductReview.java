package com.ruoyi.mall.review.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 商品评价及订单项快照。 */
public class MallProductReview implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long reviewId;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long memberId;
    private Long spuId;
    private Long skuId;
    private String productName;
    private String skuName;
    private String productImage;
    private Integer rating;
    private String content;
    private String anonymous;
    private String status;
    private String reviewerName;
    private String memberPhone;
    private String auditBy;
    private LocalDateTime auditTime;
    private String auditRemark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<String> imageUrls = new ArrayList<>();

    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long value) { reviewId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long value) { orderItemId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long value) { spuId = value; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long value) { skuId = value; }
    public String getProductName() { return productName; }
    public void setProductName(String value) { productName = value; }
    public String getSkuName() { return skuName; }
    public void setSkuName(String value) { skuName = value; }
    public String getProductImage() { return productImage; }
    public void setProductImage(String value) { productImage = value; }
    public Integer getRating() { return rating; }
    public void setRating(Integer value) { rating = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getAnonymous() { return anonymous; }
    public void setAnonymous(String value) { anonymous = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String value) { reviewerName = value; }
    public String getMemberPhone() { return memberPhone; }
    public void setMemberPhone(String value) { memberPhone = value; }
    public String getAuditBy() { return auditBy; }
    public void setAuditBy(String value) { auditBy = value; }
    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime value) { auditTime = value; }
    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String value) { auditRemark = value; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { createTime = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { updateTime = value; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> value) { imageUrls = value == null ? new ArrayList<>() : value; }
}
