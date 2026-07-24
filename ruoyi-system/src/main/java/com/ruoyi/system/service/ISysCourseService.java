package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysCourse;

/**
 * 课程管理Service接口
 *
 * @author ruoyi
 */
public interface ISysCourseService
{
    public SysCourse selectCourseById(Long courseId);

    public List<SysCourse> selectCourseList(SysCourse course);

    public int insertCourse(SysCourse course);

    public int updateCourse(SysCourse course);

    public int deleteCourseByIds(Long[] courseIds);

    public int deleteCourseById(Long courseId);
}
