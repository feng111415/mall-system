package com.ruoyi.mall.service;

import java.util.List;
import com.ruoyi.mall.domain.product.MallBrand;
import com.ruoyi.mall.domain.product.MallCategory;
import com.ruoyi.mall.domain.product.MallSpec;
import com.ruoyi.mall.domain.product.MallSpu;

public interface IMallProductService
{
    List<MallCategory> selectCategories(MallCategory query);
    MallCategory selectCategoryById(Long categoryId);
    int saveCategory(MallCategory category);
    int deleteCategory(Long categoryId);

    List<MallBrand> selectBrands(MallBrand query);
    MallBrand selectBrandById(Long brandId);
    int saveBrand(MallBrand brand);
    int deleteBrand(Long brandId);

    List<MallSpu> selectProducts(MallSpu query, boolean publishedOnly);
    MallSpu selectProductDetail(Long spuId, boolean publishedOnly);
    int saveProduct(MallSpu spu);
    int updatePublishStatus(Long spuId, String publishStatus, String updateBy);
    int deleteProduct(Long spuId);
    List<MallSpec> selectSpecs(Long categoryId);
}
