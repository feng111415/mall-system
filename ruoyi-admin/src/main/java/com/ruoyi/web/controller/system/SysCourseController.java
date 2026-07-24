package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysCourse;
import com.ruoyi.system.service.ISysCourseService;

/**
 * 课程管理Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/course")
public class SysCourseController extends BaseController
{
    @Autowired
    private ISysCourseService courseService;

    @PreAuthorize("@ss.hasPermi('system:course:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysCourse course)
    {
        startPage();
        List<SysCourse> list = courseService.selectCourseList(course);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('system:course:export')")
    @Log(title = "课程管理", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysCourse course)
    {
        List<SysCourse> list = courseService.selectCourseList(course);
        ExcelUtil<SysCourse> util = new ExcelUtil<SysCourse>(SysCourse.class);
        util.exportExcel(response, list, "课程数据");
    }

    @PreAuthorize("@ss.hasPermi('system:course:query')")
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId)
    {
        return success(courseService.selectCourseById(courseId));
    }

    @PreAuthorize("@ss.hasPermi('system:course:add')")
    @Log(title = "课程管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysCourse course)
    {
        course.setCreateBy(getUsername());
        return toAjax(courseService.insertCourse(course));
    }

    @PreAuthorize("@ss.hasPermi('system:course:edit')")
    @Log(title = "课程管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysCourse course)
    {
        course.setUpdateBy(getUsername());
        return toAjax(courseService.updateCourse(course));
    }

    @PreAuthorize("@ss.hasPermi('system:course:remove')")
    @Log(title = "课程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds)
    {
        return toAjax(courseService.deleteCourseByIds(courseIds));
    }
}
