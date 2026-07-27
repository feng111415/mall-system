package com.ruoyi.mall.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.domain.product.MallBrand;
import com.ruoyi.mall.domain.product.MallCategory;
import com.ruoyi.mall.domain.product.MallSpu;
import com.ruoyi.mall.service.IMallProductService;

@RestController
@RequestMapping("/mall/catalog")
public class MallProductAdminController extends BaseController
{
    private final IMallProductService service;

    public MallProductAdminController(IMallProductService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:category:list')")
    @GetMapping("/categories")
    public AjaxResult categories(MallCategory query) { return success(service.selectCategories(query)); }

    @PreAuthorize("@ss.hasPermi('mall:category:add')")
    @Log(title = "商品分类", businessType = BusinessType.INSERT)
    @PostMapping("/categories")
    public AjaxResult saveCategory(@RequestBody MallCategory value)
    {
        value.setCreateBy(getUsername());
        return toAjax(service.saveCategory(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:category:edit')")
    @Log(title = "商品分类", businessType = BusinessType.UPDATE)
    @PutMapping("/categories")
    public AjaxResult editCategory(@RequestBody MallCategory value)
    {
        value.setUpdateBy(getUsername());
        return toAjax(service.saveCategory(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:category:remove')")
    @Log(title = "商品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/categories/{id}")
    public AjaxResult deleteCategory(@PathVariable Long id) { return toAjax(service.deleteCategory(id)); }

    @PreAuthorize("@ss.hasPermi('mall:brand:list')")
    @GetMapping("/brands")
    public AjaxResult brands(MallBrand query) { return success(service.selectBrands(query)); }

    @PreAuthorize("@ss.hasPermi('mall:brand:add')")
    @Log(title = "商品品牌", businessType = BusinessType.INSERT)
    @PostMapping("/brands")
    public AjaxResult saveBrand(@RequestBody MallBrand value)
    {
        value.setCreateBy(getUsername());
        return toAjax(service.saveBrand(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:brand:edit')")
    @Log(title = "商品品牌", businessType = BusinessType.UPDATE)
    @PutMapping("/brands")
    public AjaxResult editBrand(@RequestBody MallBrand value)
    {
        value.setUpdateBy(getUsername());
        return toAjax(service.saveBrand(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:brand:remove')")
    @Log(title = "商品品牌", businessType = BusinessType.DELETE)
    @DeleteMapping("/brands/{id}")
    public AjaxResult deleteBrand(@PathVariable Long id) { return toAjax(service.deleteBrand(id)); }

    @PreAuthorize("@ss.hasPermi('mall:product:list')")
    @GetMapping("/products")
    public AjaxResult products(MallSpu query) { return success(service.selectProducts(query, false)); }

    @PreAuthorize("@ss.hasPermi('mall:product:query')")
    @GetMapping("/products/{id}")
    public AjaxResult product(@PathVariable Long id) { return success(service.selectProductDetail(id, false)); }

    @PreAuthorize("@ss.hasPermi('mall:product:add')")
    @Log(title = "商品", businessType = BusinessType.INSERT)
    @PostMapping("/products")
    public AjaxResult saveProduct(@RequestBody MallSpu value)
    {
        value.setCreateBy(getUsername());
        return toAjax(service.saveProduct(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:edit')")
    @Log(title = "商品", businessType = BusinessType.UPDATE)
    @PutMapping("/products")
    public AjaxResult editProduct(@RequestBody MallSpu value)
    {
        value.setUpdateBy(getUsername());
        return toAjax(service.saveProduct(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:publish')")
    @Log(title = "商品上下架", businessType = BusinessType.UPDATE)
    @PutMapping("/products/{id}/publish/{status}")
    public AjaxResult publish(@PathVariable Long id, @PathVariable String status)
    {
        return toAjax(service.updatePublishStatus(id, status, getUsername()));
    }

    @PreAuthorize("@ss.hasPermi('mall:product:remove')")
    @Log(title = "商品", businessType = BusinessType.DELETE)
    @DeleteMapping("/products/{id}")
    public AjaxResult deleteProduct(@PathVariable Long id) { return toAjax(service.deleteProduct(id)); }
}
