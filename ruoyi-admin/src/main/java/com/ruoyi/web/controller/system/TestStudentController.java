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
import com.ruoyi.system.domain.TestStudent;
import com.ruoyi.system.service.ITestStudentService;

@RestController
@RequestMapping("/system/testStudent")
public class TestStudentController extends BaseController
{
    @Autowired
    private ITestStudentService testStudentService;

    @GetMapping("/list")
    public TableDataInfo list(TestStudent testStudent)
    {
        startPage();
        List<TestStudent> list =
            testStudentService.selectTestStudentList(testStudent);
        return getDataTable(list);
    }

    @GetMapping("/{studentId}")
    public AjaxResult getInfo(@PathVariable Long studentId)
    {
        return success(testStudentService.selectTestStudentById(studentId));
    }

    @PostMapping
    public AjaxResult add(@RequestBody TestStudent testStudent)
    {
        return toAjax(testStudentService.insertTestStudent(testStudent));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody TestStudent testStudent)
    {
        return toAjax(testStudentService.updateTestStudent(testStudent));
    }

    @DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds)
    {
        return toAjax(testStudentService.deleteTestStudentByIds(studentIds));
    }
}