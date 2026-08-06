package com.ruoyi.mall.web.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mall.content.domain.MallHomeContent;
import com.ruoyi.mall.content.domain.MallHomeContentQuery;
import com.ruoyi.mall.content.service.IMallHomeContentService;

@RestController
@RequestMapping("/mall/content/homepage")
public class MallHomeContentAdminController extends BaseController
{
    private final IMallHomeContentService service;

    public MallHomeContentAdminController(IMallHomeContentService service) { this.service = service; }

    @PreAuthorize("@ss.hasPermi('mall:content:list')")
    @GetMapping
    public TableDataInfo list(MallHomeContentQuery query)
    {
        startPage();
        List<MallHomeContent> rows = service.selectAdminContentList(query);
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('mall:content:query')")
    @GetMapping("/{id}")
    public AjaxResult detail(@PathVariable Long id) { return success(service.selectContentById(id)); }

    @PreAuthorize("@ss.hasAnyPermi('mall:content:add,mall:content:publish')")
    @Log(title = "首页内容", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody MallHomeContent value)
    {
        value.setCreateBy(getUsername());
        return toAjax(service.saveContent(value));
    }

    @PreAuthorize("@ss.hasAnyPermi('mall:content:edit,mall:content:publish')")
    @Log(title = "首页内容", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody MallHomeContent value)
    {
        value.setUpdateBy(getUsername());
        return toAjax(service.saveContent(value));
    }

    @PreAuthorize("@ss.hasPermi('mall:content:disable')")
    @Log(title = "首页内容停用", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/disable")
    public AjaxResult disable(@PathVariable Long id) { return toAjax(service.disableContent(id, getUsername())); }

}
