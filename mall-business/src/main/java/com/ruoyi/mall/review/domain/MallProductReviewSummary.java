package com.ruoyi.mall.review.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallProductReviewSummary implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Integer reviewCount;
    private BigDecimal averageRating;
    private Integer fiveStarCount;
    private Integer fourStarCount;
    private Integer threeStarCount;
    private Integer twoStarCount;
    private Integer oneStarCount;

    public Integer getReviewCount() { return reviewCount == null ? 0 : reviewCount; }
    public void setReviewCount(Integer value) { reviewCount = value; }
    public BigDecimal getAverageRating() { return averageRating == null ? BigDecimal.ZERO : averageRating; }
    public void setAverageRating(BigDecimal value) { averageRating = value; }
    public Integer getFiveStarCount() { return fiveStarCount == null ? 0 : fiveStarCount; }
    public void setFiveStarCount(Integer value) { fiveStarCount = value; }
    public Integer getFourStarCount() { return fourStarCount == null ? 0 : fourStarCount; }
    public void setFourStarCount(Integer value) { fourStarCount = value; }
    public Integer getThreeStarCount() { return threeStarCount == null ? 0 : threeStarCount; }
    public void setThreeStarCount(Integer value) { threeStarCount = value; }
    public Integer getTwoStarCount() { return twoStarCount == null ? 0 : twoStarCount; }
    public void setTwoStarCount(Integer value) { twoStarCount = value; }
    public Integer getOneStarCount() { return oneStarCount == null ? 0 : oneStarCount; }
    public void setOneStarCount(Integer value) { oneStarCount = value; }
}
