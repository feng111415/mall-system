package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysCourse;

/**
 * 课程管理Mapper接口
 *
 * @author ruoyi
 */
public interface SysCourseMapper
{
    public SysCourse selectCourseById(Long courseId);

    public List<SysCourse> selectCourseList(SysCourse course);

    public int insertCourse(SysCourse course);

    public int updateCourse(SysCourse course);

    public int deleteCourseById(Long courseId);

    public int deleteCourseByIds(Long[] courseIds);
}
