package com.ruoyi.web.controller.piano;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.piano.domain.dto.PianoPortalSubmitRequest;
import com.ruoyi.piano.domain.vo.PianoPortalHomeworkVo;
import com.ruoyi.piano.service.IPianoHomeworkService;

/**
 * 钢琴作业学生端提交入口。
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/piano/portal")
public class PianoStudentPortalController extends BaseController
{
    @Autowired
    private IPianoHomeworkService pianoHomeworkService;

    @GetMapping("/homework/{submitCode}")
    public AjaxResult getHomework(@PathVariable("submitCode") String submitCode)
    {
        return success(pianoHomeworkService.selectPianoPortalHomeworkBySubmitCode(submitCode));
    }

    @GetMapping("/history/{submitCode}")
    public AjaxResult history(@PathVariable("submitCode") String submitCode)
    {
        PianoPortalHomeworkVo homework = pianoHomeworkService.selectPianoPortalHomeworkBySubmitCode(submitCode);
        return success(homework.getSubmissionHistory());
    }

    @PostMapping("/submission")
    public AjaxResult submit(@Valid @RequestBody PianoPortalSubmitRequest submitRequest)
    {
        return toAjax(pianoHomeworkService.submitPianoPortalHomework(submitRequest));
    }
}
