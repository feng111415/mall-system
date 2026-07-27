package com.ruoyi.mall.domain.product;

import com.ruoyi.common.core.domain.BaseEntity;

public class MallBrand extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long brandId;
    private String brandName;
    private String logoUrl;
    private String description;
    private Integer sortNo;
    private String status;
    private String delFlag;

    public Long getBrandId() { return brandId; }
    public void setBrandId(Long brandId) { this.brandId = brandId; }
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDelFlag() { return delFlag; }
    public void setDelFlag(String delFlag) { this.delFlag = delFlag; }
}
