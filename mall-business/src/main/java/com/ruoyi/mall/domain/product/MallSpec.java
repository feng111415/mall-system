package com.ruoyi.mall.domain.product;

import java.util.List;

public class MallSpec
{
    private Long specId;
    private Long categoryId;
    private String specName;
    private Integer sortNo;
    private String status;
    private List<MallSpecValue> values;

    public Long getSpecId() { return specId; }
    public void setSpecId(Long specId) { this.specId = specId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getSpecName() { return specName; }
    public void setSpecName(String specName) { this.specName = specName; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer sortNo) { this.sortNo = sortNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<MallSpecValue> getValues() { return values; }
    public void setValues(List<MallSpecValue> values) { this.values = values; }
}
