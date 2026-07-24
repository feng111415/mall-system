package com.ruoyi.system.domain;

import com.ruoyi.common.core.domain.BaseEntity;

public class KbCategory extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    private Long categoryId;
    private String categoryName; 
    private Integer orderNum;
    private String status;
    public Long getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    public String getCategoryName() {
        return categoryName;
    }
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    public Integer getOrderNum() {
        return orderNum;
    }
    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
   
}
