package com.ruoyi.mall.service.impl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.domain.product.MallBrand;
import com.ruoyi.mall.domain.product.MallCategory;
import com.ruoyi.mall.domain.product.MallProductMedia;
import com.ruoyi.mall.domain.product.MallSku;
import com.ruoyi.mall.domain.product.MallSpec;
import com.ruoyi.mall.domain.product.MallSpu;
import com.ruoyi.mall.mapper.MallProductMapper;
import com.ruoyi.mall.service.IMallProductService;

@Service
public class MallProductServiceImpl implements IMallProductService
{
    private final MallProductMapper productMapper;

    public MallProductServiceImpl(MallProductMapper productMapper)
    {
        this.productMapper = productMapper;
    }

    @Override
    public List<MallCategory> selectCategories(MallCategory query) { return productMapper.selectCategoryList(query); }

    @Override
    public MallCategory selectCategoryById(Long categoryId) { return productMapper.selectCategoryById(categoryId); }

    @Override
    public int saveCategory(MallCategory category)
    {
        if (StringUtils.isBlank(category.getCategoryName())) throw new ServiceException("分类名称不能为空");
        if (category.getParentId() == null) category.setParentId(0L);
        if (category.getLevel() == null) category.setLevel(1);
        if (category.getSortNo() == null) category.setSortNo(0);
        if (StringUtils.isBlank(category.getStatus())) category.setStatus("0");
        return category.getCategoryId() == null ? productMapper.insertCategory(category) : productMapper.updateCategory(category);
    }

    @Override
    public int deleteCategory(Long categoryId) { return productMapper.deleteCategory(categoryId); }

    @Override
    public List<MallBrand> selectBrands(MallBrand query) { return productMapper.selectBrandList(query); }

    @Override
    public MallBrand selectBrandById(Long brandId) { return productMapper.selectBrandById(brandId); }

    @Override
    public int saveBrand(MallBrand brand)
    {
        if (StringUtils.isBlank(brand.getBrandName())) throw new ServiceException("品牌名称不能为空");
        if (brand.getSortNo() == null) brand.setSortNo(0);
        if (StringUtils.isBlank(brand.getStatus())) brand.setStatus("0");
        return brand.getBrandId() == null ? productMapper.insertBrand(brand) : productMapper.updateBrand(brand);
    }

    @Override
    public int deleteBrand(Long brandId) { return productMapper.deleteBrand(brandId); }

    @Override
    public List<MallSpu> selectProducts(MallSpu query, boolean publishedOnly)
    {
        if (publishedOnly) query.setPublishStatus("1");
        return productMapper.selectSpuList(query);
    }

    @Override
    public MallSpu selectProductDetail(Long spuId, boolean publishedOnly)
    {
        MallSpu spu = productMapper.selectSpuById(spuId, publishedOnly);
        if (spu == null) return null;
        spu.setSkuList(productMapper.selectSkuListBySpuId(spuId));
        spu.setMediaList(productMapper.selectMediaListBySpuId(spuId));
        return spu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveProduct(MallSpu spu)
    {
        validateProduct(spu);
        calculatePriceRange(spu);
        int rows;
        if (spu.getSpuId() == null)
        {
            rows = productMapper.insertSpu(spu);
        }
        else
        {
            rows = productMapper.updateSpu(spu);
            productMapper.deleteSkuBySpuId(spu.getSpuId());
            productMapper.deleteMediaBySpuId(spu.getSpuId());
        }
        for (MallSku sku : spu.getSkuList())
        {
            sku.setSpuId(spu.getSpuId());
            sku.setCreateBy(StringUtils.isNotBlank(spu.getCreateBy()) ? spu.getCreateBy() : spu.getUpdateBy());
            productMapper.insertSku(sku);
        }
        for (MallProductMedia media : safeList(spu.getMediaList()))
        {
            media.setSpuId(spu.getSpuId());
            productMapper.insertMedia(media);
        }
        return rows;
    }

    @Override
    public int updatePublishStatus(Long spuId, String publishStatus, String updateBy)
    {
        if (!"0".equals(publishStatus) && !"1".equals(publishStatus)) throw new ServiceException("上架状态不合法");
        return productMapper.updatePublishStatus(spuId, publishStatus, updateBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProduct(Long spuId)
    {
        productMapper.deleteSkuBySpuId(spuId);
        productMapper.deleteMediaBySpuId(spuId);
        return productMapper.deleteSpu(spuId);
    }

    @Override
    public List<MallSpec> selectSpecs(Long categoryId)
    {
        List<MallSpec> specs = productMapper.selectSpecList(categoryId);
        specs.forEach(spec -> spec.setValues(productMapper.selectSpecValueList(spec.getSpecId())));
        return specs;
    }

    private void validateProduct(MallSpu spu)
    {
        if (StringUtils.isBlank(spu.getProductName())) throw new ServiceException("商品名称不能为空");
        if (StringUtils.isBlank(spu.getSpuCode())) throw new ServiceException("SPU 编码不能为空");
        if (spu.getCategoryId() == null) throw new ServiceException("商品分类不能为空");
        if (spu.getSkuList() == null || spu.getSkuList().isEmpty()) throw new ServiceException("商品至少需要一个 SKU");
        for (MallSku sku : spu.getSkuList())
        {
            if (StringUtils.isBlank(sku.getSkuCode()) || sku.getPrice() == null)
                throw new ServiceException("SKU 编码和销售价不能为空");
            if (sku.getPrice().signum() < 0) throw new ServiceException("SKU 销售价不能为负数");
            if (sku.getAvailableStock() == null) sku.setAvailableStock(0);
            if (sku.getAvailableStock() < 0) throw new ServiceException("SKU 可售数量不能为负数");
            if (sku.getStatus() == null) sku.setStatus("1");
        }
        if (spu.getPublishStatus() == null) spu.setPublishStatus("0");
        if (spu.getSortNo() == null) spu.setSortNo(0);
    }

    private void calculatePriceRange(MallSpu spu)
    {
        BigDecimal min = spu.getSkuList().stream().map(MallSku::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal max = spu.getSkuList().stream().map(MallSku::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        spu.setPriceMin(min);
        spu.setPriceMax(max);
    }

    private static <T> List<T> safeList(List<T> list) { return list == null ? Collections.emptyList() : list; }
}
