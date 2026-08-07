package com.ruoyi.mall.review.domain.dto;

import java.io.Serializable;

public class MallProductReviewAuditRequest implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String status;
    private String remark;

    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getRemark() { return remark; }
    public void setRemark(String value) { remark = value; }
}
