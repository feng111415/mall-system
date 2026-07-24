package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.SysCourse;
import com.ruoyi.system.mapper.SysCourseMapper;
import com.ruoyi.system.service.ISysCourseService;

/**
 * 课程管理Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysCourseServiceImpl implements ISysCourseService
{
    @Autowired
    private SysCourseMapper courseMapper;

    @Override
    public SysCourse selectCourseById(Long courseId)
    {
        return courseMapper.selectCourseById(courseId);
    }

    @Override
    public List<SysCourse> selectCourseList(SysCourse course)
    {
        return courseMapper.selectCourseList(course);
    }

    @Override
    public int insertCourse(SysCourse course)
    {
        return courseMapper.insertCourse(course);
    }

    @Override
    public int updateCourse(SysCourse course)
    {
        return courseMapper.updateCourse(course);
    }

    @Override
    public int deleteCourseByIds(Long[] courseIds)
    {
        return courseMapper.deleteCourseByIds(courseIds);
    }

    @Override
    public int deleteCourseById(Long courseId)
    {
        return courseMapper.deleteCourseById(courseId);
    }
}
