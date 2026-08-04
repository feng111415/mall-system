package com.ruoyi.mall.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.domain.product.MallBrand;
import com.ruoyi.mall.domain.product.MallCategory;
import com.ruoyi.mall.domain.product.MallProductMedia;
import com.ruoyi.mall.domain.product.MallSku;
import com.ruoyi.mall.domain.product.MallSpec;
import com.ruoyi.mall.domain.product.MallSpecValue;
import com.ruoyi.mall.domain.product.MallSpu;
import com.ruoyi.mall.domain.product.dto.MallCatalogProductQuery;

public interface MallProductMapper
{
    List<MallCategory> selectCategoryList(MallCategory query);
    MallCategory selectCategoryById(Long categoryId);
    int insertCategory(MallCategory category);
    int updateCategory(MallCategory category);
    int deleteCategory(Long categoryId);

    List<MallBrand> selectBrandList(MallBrand query);
    MallBrand selectBrandById(Long brandId);
    int insertBrand(MallBrand brand);
    int updateBrand(MallBrand brand);
    int deleteBrand(Long brandId);

    List<MallSpu> selectSpuList(MallSpu query);
    List<MallSpu> selectPublishedSpuList(MallCatalogProductQuery query);
    MallSpu selectSpuById(@Param("spuId") Long spuId, @Param("publishedOnly") boolean publishedOnly);
    int insertSpu(MallSpu spu);
    int updateSpu(MallSpu spu);
    int updatePublishStatus(@Param("spuId") Long spuId, @Param("publishStatus") String publishStatus,
        @Param("updateBy") String updateBy);
    int deleteSpu(Long spuId);

    List<MallSku> selectSkuListBySpuId(Long spuId);
    int insertSku(MallSku sku);
    int updateSku(MallSku sku);
    int countSkuOperationalData(Long skuId);
    int deleteSku(Long skuId);
    int deleteSkuBySpuId(Long spuId);
    List<MallProductMedia> selectMediaListBySpuId(Long spuId);
    int insertMedia(MallProductMedia media);
    int deleteMediaBySpuId(Long spuId);

    List<MallSpec> selectSpecList(Long categoryId);
    List<MallSpecValue> selectSpecValueList(Long specId);
}
