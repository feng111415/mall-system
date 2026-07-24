package com.ruoyi.piano.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.piano.domain.PianoHomework;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;
import com.ruoyi.piano.domain.dto.PianoPortalSubmitRequest;
import com.ruoyi.piano.domain.vo.PianoDashboardVo;
import com.ruoyi.piano.domain.vo.PianoPortalHomeworkVo;
import com.ruoyi.piano.mapper.PianoHomeworkMapper;
import com.ruoyi.piano.mapper.PianoHomeworkSubmissionMapper;
import com.ruoyi.piano.mapper.PianoStudentMapper;
import com.ruoyi.piano.service.IPianoHomeworkService;

/**
 * 钢琴课后作业Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PianoHomeworkServiceImpl implements IPianoHomeworkService
{
    @Autowired
    private PianoHomeworkMapper pianoHomeworkMapper;

    @Autowired
    private PianoStudentMapper pianoStudentMapper;

    @Autowired
    private PianoHomeworkSubmissionMapper pianoHomeworkSubmissionMapper;

    @Override
    public PianoHomework selectPianoHomeworkById(Long homeworkId)
    {
        return pianoHomeworkMapper.selectPianoHomeworkById(homeworkId);
    }

    @Override
    public PianoHomework selectPianoHomeworkBySubmitCode(String submitCode)
    {
        return pianoHomeworkMapper.selectPianoHomeworkBySubmitCode(normalizeSubmitCode(submitCode));
    }

    @Override
    public PianoPortalHomeworkVo selectPianoPortalHomeworkBySubmitCode(String submitCode)
    {
        PianoHomework homework = selectRequiredHomeworkBySubmitCode(submitCode);
        List<PianoHomeworkSubmission> history = pianoHomeworkSubmissionMapper.selectPianoHomeworkSubmissionHistory(homework.getHomeworkId());

        PianoPortalHomeworkVo vo = new PianoPortalHomeworkVo();
        vo.setHomeworkId(homework.getHomeworkId());
        vo.setSubmitCode(homework.getSubmitCode());
        vo.setStudentName(homework.getStudentName());
        vo.setHomeworkTitle(homework.getHomeworkTitle());
        vo.setPracticeContent(homework.getPracticeContent());
        vo.setPracticeRequirement(homework.getPracticeRequirement());
        vo.setSubmitDeadline(homework.getSubmitDeadline());
        vo.setHomeworkStatus(homework.getHomeworkStatus());
        vo.setSubmissionHistory(history);
        vo.setLatestSubmission(findLatestSubmission(history));
        return vo;
    }

    @Override
    public List<PianoHomework> selectPianoHomeworkList(PianoHomework pianoHomework)
    {
        return pianoHomeworkMapper.selectPianoHomeworkList(pianoHomework);
    }

    @Override
    public int insertPianoHomework(PianoHomework pianoHomework)
    {
        if (StringUtils.isEmpty(pianoHomework.getHomeworkStatus()))
        {
            pianoHomework.setHomeworkStatus("0");
        }
        if (StringUtils.isEmpty(pianoHomework.getSubmitCode()))
        {
            pianoHomework.setSubmitCode(generateSubmitCode());
        }
        return pianoHomeworkMapper.insertPianoHomework(pianoHomework);
    }

    @Override
    public int updatePianoHomework(PianoHomework pianoHomework)
    {
        return pianoHomeworkMapper.updatePianoHomework(pianoHomework);
    }

    @Override
    public int deletePianoHomeworkByIds(Long[] homeworkIds)
    {
        return pianoHomeworkMapper.deletePianoHomeworkByIds(homeworkIds);
    }

    @Override
    public int deletePianoHomeworkById(Long homeworkId)
    {
        return pianoHomeworkMapper.deletePianoHomeworkById(homeworkId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitPianoPortalHomework(PianoPortalSubmitRequest submitRequest)
    {
        PianoHomework homework = selectRequiredHomeworkBySubmitCode(submitRequest.getSubmitCode());
        if (StringUtils.equals("3", homework.getHomeworkStatus()))
        {
            throw new ServiceException("作业已完成，暂不能继续提交");
        }

        PianoHomeworkSubmission submission = new PianoHomeworkSubmission();
        submission.setHomeworkId(homework.getHomeworkId());
        submission.setStudentId(homework.getStudentId());
        submission.setSubmitContent(submitRequest.getSubmitContent());
        submission.setAttachmentUrl(submitRequest.getAttachmentUrl());

        Integer maxAttemptNo = pianoHomeworkSubmissionMapper.selectMaxAttemptNoByHomeworkId(homework.getHomeworkId());
        int attemptNo = maxAttemptNo == null ? 1 : maxAttemptNo + 1;
        submission.setAttemptNo(attemptNo);
        submission.setSubmitType(attemptNo == 1 ? "0" : "1");
        submission.setSubmitTime(new java.util.Date());
        submission.setOverdueFlag(homework.getSubmitDeadline() != null && submission.getSubmitTime().after(homework.getSubmitDeadline()) ? "1" : "0");
        submission.setReviewStatus("0");
        submission.setCorrectionRequired("0");
        submission.setLatestFlag("1");
        submission.setCreateBy("portal");

        pianoHomeworkSubmissionMapper.clearLatestFlagByHomeworkId(homework.getHomeworkId());
        int rows = pianoHomeworkSubmissionMapper.insertPianoHomeworkSubmission(submission);
        pianoHomeworkMapper.updatePianoHomeworkStatus(homework.getHomeworkId(), "1");
        return rows;
    }

    @Override
    public PianoDashboardVo selectPianoDashboard()
    {
        PianoDashboardVo dashboard = new PianoDashboardVo();

        dashboard.setStudentCount(defaultLong(pianoStudentMapper.selectActivePianoStudentCount()));
        dashboard.setWeekHomeworkCount(defaultLong(pianoHomeworkMapper.selectWeekHomeworkCount()));
        dashboard.setPendingReviewCount(defaultLong(pianoHomeworkMapper.selectPianoHomeworkCountByStatus("1")));
        dashboard.setCorrectionCount(defaultLong(pianoHomeworkMapper.selectPianoHomeworkCountByStatus("2")));
        dashboard.setOverdueCount(defaultLong(pianoHomeworkMapper.selectOverdueUnfinishedHomeworkCount()));
        dashboard.setTodoHomeworks(pianoHomeworkMapper.selectDashboardTodoHomeworks());
        dashboard.setRecentSubmissions(pianoHomeworkSubmissionMapper.selectDashboardRecentSubmissions());
        return dashboard;
    }

    private Long defaultLong(Long value)
    {
        return value == null ? 0L : value;
    }

    private PianoHomework selectRequiredHomeworkBySubmitCode(String submitCode)
    {
        PianoHomework homework = pianoHomeworkMapper.selectPianoHomeworkBySubmitCode(normalizeSubmitCode(submitCode));
        if (homework == null)
        {
            throw new ServiceException("提交码无效，请核对后重试");
        }
        return homework;
    }

    private PianoHomeworkSubmission findLatestSubmission(List<PianoHomeworkSubmission> history)
    {
        if (history == null || history.isEmpty())
        {
            return null;
        }
        for (PianoHomeworkSubmission submission : history)
        {
            if (StringUtils.equals("1", submission.getLatestFlag()))
            {
                return submission;
            }
        }
        return history.get(0);
    }

    private String normalizeSubmitCode(String submitCode)
    {
        return submitCode == null ? null : submitCode.trim().toUpperCase();
    }

    private String generateSubmitCode()
    {
        for (int i = 0; i < 10; i++)
        {
            String submitCode = "P" + IdUtils.fastSimpleUUID().substring(0, 9).toUpperCase();
            if (defaultLong(pianoHomeworkMapper.selectPianoHomeworkCountBySubmitCode(submitCode)) == 0L)
            {
                return submitCode;
            }
        }
        throw new ServiceException("提交码生成失败，请稍后重试");
    }

}
