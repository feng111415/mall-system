package com.ruoyi.mall.review.domain;

import java.io.Serializable;

public class MallProductReviewQuery implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String status;
    private String keyword;
    private Long spuId;

    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String value) { keyword = value; }
    public Long getSpuId() { return spuId; }
    public void setSpuId(Long value) { spuId = value; }
}
