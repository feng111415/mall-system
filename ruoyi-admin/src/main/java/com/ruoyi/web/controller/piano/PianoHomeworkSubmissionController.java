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
import com.ruoyi.piano.domain.PianoHomeworkSubmission;
import com.ruoyi.piano.domain.dto.PianoHomeworkReviewRequest;
import com.ruoyi.piano.service.IPianoHomeworkSubmissionService;

/**
 * 钢琴作业提交点评Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/piano/submission")
public class PianoHomeworkSubmissionController extends BaseController
{
    @Autowired
    private IPianoHomeworkSubmissionService pianoHomeworkSubmissionService;

    @PreAuthorize("@ss.hasPermi('piano:submission:list')")
    @GetMapping("/list")
    public TableDataInfo list(PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        startPage();
        List<PianoHomeworkSubmission> list = pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionList(pianoHomeworkSubmission);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:export')")
    @Log(title = "钢琴作业提交点评", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        List<PianoHomeworkSubmission> list = pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionList(pianoHomeworkSubmission);
        ExcelUtil<PianoHomeworkSubmission> util = new ExcelUtil<PianoHomeworkSubmission>(PianoHomeworkSubmission.class);
        util.exportExcel(response, list, "钢琴作业提交点评数据");
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:query')")
    @GetMapping(value = "/{submissionId}")
    public AjaxResult getInfo(@PathVariable("submissionId") Long submissionId)
    {
        return success(pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionById(submissionId));
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:list')")
    @GetMapping("/history/{homeworkId}")
    public AjaxResult history(@PathVariable("homeworkId") Long homeworkId)
    {
        return success(pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionHistory(homeworkId));
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:add')")
    @Log(title = "钢琴作业提交点评", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Valid @RequestBody PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        pianoHomeworkSubmission.setCreateBy(getUsername());
        return toAjax(pianoHomeworkSubmissionService.insertPianoHomeworkSubmission(pianoHomeworkSubmission));
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:edit')")
    @Log(title = "钢琴作业提交点评", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Valid @RequestBody PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        pianoHomeworkSubmission.setUpdateBy(getUsername());
        return toAjax(pianoHomeworkSubmissionService.updatePianoHomeworkSubmission(pianoHomeworkSubmission));
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:review')")
    @Log(title = "钢琴作业提交点评", businessType = BusinessType.UPDATE)
    @PutMapping("/review")
    public AjaxResult review(@Valid @RequestBody PianoHomeworkReviewRequest reviewRequest)
    {
        return toAjax(pianoHomeworkSubmissionService.reviewPianoHomeworkSubmission(reviewRequest));
    }

    @PreAuthorize("@ss.hasPermi('piano:submission:remove')")
    @Log(title = "钢琴作业提交点评", businessType = BusinessType.DELETE)
    @DeleteMapping("/{submissionIds}")
    public AjaxResult remove(@PathVariable Long[] submissionIds)
    {
        return toAjax(pianoHomeworkSubmissionService.deletePianoHomeworkSubmissionByIds(submissionIds));
    }
}
