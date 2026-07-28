package com.ruoyi.mall.web.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mall.domain.product.MallCategory;
import com.ruoyi.mall.domain.product.dto.MallCatalogProductQuery;
import com.ruoyi.mall.service.IMallProductService;

@Anonymous
@RestController
@RequestMapping("/api/mall/catalog")
public class MallProductPortalController {
    private final IMallProductService service;
    public MallProductPortalController(IMallProductService service) { this.service = service; }
    @GetMapping("/categories") public AjaxResult categories() { MallCategory q=new MallCategory(); q.setStatus("0"); return AjaxResult.success(service.selectCategories(q)); }
    @GetMapping("/products") public AjaxResult products(@Valid MallCatalogProductQuery query) { return AjaxResult.success(service.selectPublishedProducts(query)); }
    @GetMapping("/products/{spuId}") public AjaxResult detail(@PathVariable Long spuId) { return AjaxResult.success(service.selectProductDetail(spuId, true)); }
}
