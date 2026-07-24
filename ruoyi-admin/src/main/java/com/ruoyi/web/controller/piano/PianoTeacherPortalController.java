package com.ruoyi.web.controller.piano;

import java.util.HashMap;
import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.piano.domain.PianoHomework;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;
import com.ruoyi.piano.domain.PianoStudent;
import com.ruoyi.piano.domain.dto.PianoHomeworkReviewRequest;
import com.ruoyi.piano.domain.dto.PianoTeacherLoginRequest;
import com.ruoyi.piano.service.IPianoHomeworkService;
import com.ruoyi.piano.service.IPianoHomeworkSubmissionService;
import com.ruoyi.piano.service.IPianoStudentService;

/**
 * 钢琴老师小程序端入口，提供手机端完整管理能力。
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/piano/teacher-portal")
public class PianoTeacherPortalController extends BaseController
{
    private static final String TEACHER_CODE_HEADER = "X-Piano-Teacher-Code";

    @Value("${piano.teacher.portal-code:}")
    private String teacherPortalCode;

    @Autowired
    private IPianoStudentService pianoStudentService;

    @Autowired
    private IPianoHomeworkService pianoHomeworkService;

    @Autowired
    private IPianoHomeworkSubmissionService pianoHomeworkSubmissionService;

    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody PianoTeacherLoginRequest loginRequest)
    {
        validateTeacherCode(loginRequest.getTeacherCode());
        return success();
    }

    @GetMapping("/students")
    public AjaxResult students(@RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode)
    {
        validateTeacherCode(teacherCode);
        return success(pianoStudentService.selectPianoStudentList(new PianoStudent()));
    }

    @PostMapping("/students")
    public AjaxResult addStudent(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @Valid @RequestBody PianoStudent pianoStudent)
    {
        validateTeacherCode(teacherCode);
        pianoStudent.setCreateBy("teacher-portal");
        return toAjax(pianoStudentService.insertPianoStudent(pianoStudent));
    }

    @PutMapping("/students/{studentId}")
    public AjaxResult editStudent(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @PathVariable("studentId") Long studentId,
        @Valid @RequestBody PianoStudent pianoStudent)
    {
        validateTeacherCode(teacherCode);
        pianoStudent.setStudentId(studentId);
        pianoStudent.setUpdateBy("teacher-portal");
        return toAjax(pianoStudentService.updatePianoStudent(pianoStudent));
    }

    @DeleteMapping("/students/{studentId}")
    public AjaxResult deleteStudent(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @PathVariable("studentId") Long studentId)
    {
        validateTeacherCode(teacherCode);
        return toAjax(pianoStudentService.deletePianoStudentById(studentId));
    }

    @GetMapping("/homeworks")
    public AjaxResult homeworks(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        PianoHomework pianoHomework)
    {
        validateTeacherCode(teacherCode);
        return success(pianoHomeworkService.selectPianoHomeworkList(pianoHomework));
    }

    @GetMapping("/homeworks/{homeworkId}")
    public AjaxResult homeworkDetail(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @PathVariable("homeworkId") Long homeworkId)
    {
        validateTeacherCode(teacherCode);
        Map<String, Object> data = new HashMap<>();
        data.put("homework", pianoHomeworkService.selectPianoHomeworkById(homeworkId));
        data.put("submissions", pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionHistory(homeworkId));
        return success(data);
    }

    @PostMapping("/homeworks")
    public AjaxResult addHomework(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @Valid @RequestBody PianoHomework pianoHomework)
    {
        validateTeacherCode(teacherCode);
        pianoHomework.setCreateBy("teacher-portal");
        pianoHomeworkService.insertPianoHomework(pianoHomework);
        return success(pianoHomeworkService.selectPianoHomeworkById(pianoHomework.getHomeworkId()));
    }

    @PutMapping("/homeworks/{homeworkId}")
    public AjaxResult editHomework(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @PathVariable("homeworkId") Long homeworkId,
        @Valid @RequestBody PianoHomework pianoHomework)
    {
        validateTeacherCode(teacherCode);
        pianoHomework.setHomeworkId(homeworkId);
        pianoHomework.setUpdateBy("teacher-portal");
        return toAjax(pianoHomeworkService.updatePianoHomework(pianoHomework));
    }

    @DeleteMapping("/homeworks/{homeworkId}")
    public AjaxResult deleteHomework(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @PathVariable("homeworkId") Long homeworkId)
    {
        validateTeacherCode(teacherCode);
        return toAjax(pianoHomeworkService.deletePianoHomeworkById(homeworkId));
    }

    @GetMapping("/submissions")
    public AjaxResult submissions(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        validateTeacherCode(teacherCode);
        return success(pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionList(pianoHomeworkSubmission));
    }

    @GetMapping("/submissions/{submissionId}")
    public AjaxResult submissionDetail(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @PathVariable("submissionId") Long submissionId)
    {
        validateTeacherCode(teacherCode);
        return success(pianoHomeworkSubmissionService.selectPianoHomeworkSubmissionById(submissionId));
    }

    @PutMapping("/submissions/review")
    public AjaxResult reviewSubmission(
        @RequestHeader(value = TEACHER_CODE_HEADER, required = false) String teacherCode,
        @Valid @RequestBody PianoHomeworkReviewRequest reviewRequest)
    {
        validateTeacherCode(teacherCode);
        return toAjax(pianoHomeworkSubmissionService.reviewPianoHomeworkSubmission(reviewRequest));
    }

    private void validateTeacherCode(String teacherCode)
    {
        if (StringUtils.isBlank(teacherPortalCode))
        {
            throw new ServiceException("老师端口令未配置，请先配置 piano.teacher.portal-code");
        }
        if (!StringUtils.equals(teacherPortalCode, teacherCode))
        {
            throw new ServiceException("老师口令不正确");
        }
    }
}
