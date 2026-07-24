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
import com.ruoyi.piano.domain.PianoHomework;
import com.ruoyi.piano.service.IPianoHomeworkService;

/**
 * 钢琴课后作业Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/piano/homework")
public class PianoHomeworkController extends BaseController
{
    @Autowired
    private IPianoHomeworkService pianoHomeworkService;

    @PreAuthorize("@ss.hasPermi('piano:homework:list')")
    @GetMapping("/list")
    public TableDataInfo list(PianoHomework pianoHomework)
    {
        startPage();
        List<PianoHomework> list = pianoHomeworkService.selectPianoHomeworkList(pianoHomework);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('piano:dashboard:list')")
    @GetMapping("/dashboard")
    public AjaxResult dashboard()
    {
        return success(pianoHomeworkService.selectPianoDashboard());
    }

    @PreAuthorize("@ss.hasPermi('piano:homework:export')")
    @Log(title = "钢琴课后作业", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PianoHomework pianoHomework)
    {
        List<PianoHomework> list = pianoHomeworkService.selectPianoHomeworkList(pianoHomework);
        ExcelUtil<PianoHomework> util = new ExcelUtil<PianoHomework>(PianoHomework.class);
        util.exportExcel(response, list, "钢琴课后作业数据");
    }

    @PreAuthorize("@ss.hasPermi('piano:homework:query')")
    @GetMapping(value = "/{homeworkId}")
    public AjaxResult getInfo(@PathVariable("homeworkId") Long homeworkId)
    {
        return success(pianoHomeworkService.selectPianoHomeworkById(homeworkId));
    }

    @PreAuthorize("@ss.hasPermi('piano:homework:add')")
    @Log(title = "钢琴课后作业", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PianoHomework pianoHomework)
    {
        pianoHomework.setCreateBy(getUsername());
        return toAjax(pianoHomeworkService.insertPianoHomework(pianoHomework));
    }

    @PreAuthorize("@ss.hasPermi('piano:homework:edit')")
    @Log(title = "钢琴课后作业", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PianoHomework pianoHomework)
    {
        pianoHomework.setUpdateBy(getUsername());
        return toAjax(pianoHomeworkService.updatePianoHomework(pianoHomework));
    }

    @PreAuthorize("@ss.hasPermi('piano:homework:remove')")
    @Log(title = "钢琴课后作业", businessType = BusinessType.DELETE)
    @DeleteMapping("/{homeworkIds}")
    public AjaxResult remove(@PathVariable Long[] homeworkIds)
    {
        return toAjax(pianoHomeworkService.deletePianoHomeworkByIds(homeworkIds));
    }
}
