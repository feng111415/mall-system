package com.ruoyi.mall.domain.product.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/** 用户端商品浏览查询条件。 */
public class MallCatalogProductQuery
{
    @Positive(message = "分类参数不合法")
    private Long categoryId;

    @Size(max = 50, message = "搜索关键词不能超过50个字符")
    private String keyword;

    /** 保留 V0.1 参数，避免已经发布的用户端调用失效。 */
    @Size(max = 50, message = "商品名称不能超过50个字符")
    private String productName;

    private String sort;

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
