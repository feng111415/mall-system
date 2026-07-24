package com.ruoyi.system.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.KbCategoryMapper;
import com.ruoyi.system.service.IKbCategoryService;
import java.util.List;
import com.ruoyi.system.domain.KbCategory;
import com.ruoyi.common.utils.DateUtils;

@Service
public class KbCategoryServiceImpl implements IKbCategoryService
{
    @Autowired
    private KbCategoryMapper kbCategoryMapper;

    @Override
    public KbCategory selectKbCategoryById(Long categoryId)
    {
        return kbCategoryMapper.selectKbCategoryById(categoryId);
    }

    @Override
    public List<KbCategory> selectKbCategoryList(KbCategory kbCategory)
    {
        return kbCategoryMapper.selectKbCategoryList(kbCategory);
    }

    @Override
public int insertKbCategory(KbCategory kbCategory)
{
    kbCategory.setCreateTime(DateUtils.getNowDate());
    return kbCategoryMapper.insertKbCategory(kbCategory);
}

@Override
public int updateKbCategory(KbCategory kbCategory)
{
    kbCategory.setUpdateTime(DateUtils.getNowDate());
    return kbCategoryMapper.updateKbCategory(kbCategory);
}

@Override
public int deleteKbCategoryById(Long categoryId)
{
    return kbCategoryMapper.deleteKbCategoryById(categoryId);
}

@Override
public int deleteKbCategoryByIds(Long[] categoryIds)
{
    return kbCategoryMapper.deleteKbCategoryByIds(categoryIds);
}

}

