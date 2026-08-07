package com.ruoyi.mall.review.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MallProductReviewProductView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private MallProductReviewSummary summary = new MallProductReviewSummary();
    private List<MallProductReview> reviews = new ArrayList<>();

    public MallProductReviewSummary getSummary() { return summary; }
    public void setSummary(MallProductReviewSummary value) { summary = value == null ? new MallProductReviewSummary() : value; }
    public List<MallProductReview> getReviews() { return reviews; }
    public void setReviews(List<MallProductReview> value) { reviews = value == null ? new ArrayList<>() : value; }
}
