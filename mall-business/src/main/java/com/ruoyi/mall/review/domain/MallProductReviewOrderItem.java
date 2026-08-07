package com.ruoyi.mall.review.domain;

import java.io.Serializable;

public class MallProductReviewOrderItem implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long orderItemId;
    private Long orderId;
    private String orderNo;
    private Long memberId;
    private Long spuId;
    private Long skuId;
    private String productName;
    private String skuName;
    private String productImage;
    private Integer quantity;
    private Long reviewId;
    private String reviewStatus;
    private boolean canReview;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long value) { orderItemId = value; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long value) { orderId = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
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
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { quantity = value; }
    public Long getReviewId() { return reviewId; }
    public void setReviewId(Long value) { reviewId = value; }
    public String getReviewStatus() { return reviewStatus; }
    public void setReviewStatus(String value) { reviewStatus = value; }
    public boolean isCanReview() { return canReview; }
    public void setCanReview(boolean value) { canReview = value; }
}
