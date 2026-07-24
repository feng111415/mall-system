package com.ruoyi.web.controller.piano;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
import com.ruoyi.piano.domain.PianoStudent;
import com.ruoyi.piano.service.IPianoStudentService;

/**
 * 钢琴学生档案Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/piano/student")
public class PianoStudentController extends BaseController
{
    @Autowired
    private IPianoStudentService pianoStudentService;

    @PreAuthorize("@ss.hasPermi('piano:student:list')")
    @GetMapping("/list")
    public TableDataInfo list(PianoStudent pianoStudent)
    {
        startPage();
        List<PianoStudent> list = pianoStudentService.selectPianoStudentList(pianoStudent);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('piano:student:export')")
    @Log(title = "钢琴学生档案", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PianoStudent pianoStudent)
    {
        List<PianoStudent> list = pianoStudentService.selectPianoStudentList(pianoStudent);
        ExcelUtil<PianoStudent> util = new ExcelUtil<PianoStudent>(PianoStudent.class);
        util.exportExcel(response, list, "钢琴学生档案数据");
    }

    @PreAuthorize("@ss.hasPermi('piano:student:query')")
    @GetMapping(value = "/{studentId}")
    public AjaxResult getInfo(@PathVariable("studentId") Long studentId)
    {
        return success(pianoStudentService.selectPianoStudentById(studentId));
    }

    @PreAuthorize("@ss.hasPermi('piano:student:list')")
    @GetMapping("/options")
    public AjaxResult options()
    {
        return success(pianoStudentService.selectPianoStudentOptions());
    }

    @PreAuthorize("@ss.hasPermi('piano:student:add')")
    @Log(title = "钢琴学生档案", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PianoStudent pianoStudent)
    {
        pianoStudent.setCreateBy(getUsername());
        return toAjax(pianoStudentService.insertPianoStudent(pianoStudent));
    }

    @PreAuthorize("@ss.hasPermi('piano:student:edit')")
    @Log(title = "钢琴学生档案", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PianoStudent pianoStudent)
    {
        pianoStudent.setUpdateBy(getUsername());
        return toAjax(pianoStudentService.updatePianoStudent(pianoStudent));
    }

    @PreAuthorize("@ss.hasPermi('piano:student:remove')")
    @Log(title = "钢琴学生档案", businessType = BusinessType.DELETE)
    @DeleteMapping("/{studentIds}")
    public AjaxResult remove(@PathVariable Long[] studentIds)
    {
        return toAjax(pianoStudentService.deletePianoStudentByIds(studentIds));
    }
}
