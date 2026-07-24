package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.KbCategory;

public interface IKbCategoryService
{
    KbCategory selectKbCategoryById(Long categoryId);

    List<KbCategory> selectKbCategoryList(KbCategory kbCategory);

    int insertKbCategory(KbCategory kbCategory);

    int updateKbCategory(KbCategory kbCategory);

    int deleteKbCategoryById(Long categoryId);

    int deleteKbCategoryByIds(Long[] categoryIds);
}