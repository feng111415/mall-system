package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.KbCategory;
import com.ruoyi.system.service.IKbCategoryService;

@RestController
@RequestMapping("/knowledge/category")
public class KbCategoryController extends BaseController
{
    @Autowired
    private IKbCategoryService kbCategoryService;

    @GetMapping("/list")
    public TableDataInfo list(KbCategory kbCategory)
    {
        startPage();
        List<KbCategory> list = kbCategoryService.selectKbCategoryList(kbCategory);
        return getDataTable(list);
    }

    @GetMapping("/{categoryId}")
    public AjaxResult getInfo(@PathVariable Long categoryId)
    {
        return success(kbCategoryService.selectKbCategoryById(categoryId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody KbCategory kbCategory)
    {
        return toAjax(kbCategoryService.insertKbCategory(kbCategory));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody KbCategory kbCategory)
    {
        return toAjax(kbCategoryService.updateKbCategory(kbCategory));
    }

    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(kbCategoryService.deleteKbCategoryByIds(categoryIds));
    }
}
