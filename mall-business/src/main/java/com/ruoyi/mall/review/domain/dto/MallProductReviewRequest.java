package com.ruoyi.mall.review.domain.dto;

import java.io.Serializable;
import java.util.List;

public class MallProductReviewRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long orderItemId;
    private Integer rating;
    private String content;
    private Boolean anonymous;
    private List<String> imageUrls;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long value) { orderItemId = value; }
    public Integer getRating() { return rating; }
    public void setRating(Integer value) { rating = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public Boolean getAnonymous() { return anonymous; }
    public void setAnonymous(Boolean value) { anonymous = value; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> value) { imageUrls = value; }
}
