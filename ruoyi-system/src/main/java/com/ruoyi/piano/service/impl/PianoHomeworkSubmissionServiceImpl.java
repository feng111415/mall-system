package com.ruoyi.piano.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.piano.domain.PianoHomework;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;
import com.ruoyi.piano.domain.dto.PianoHomeworkReviewRequest;
import com.ruoyi.piano.mapper.PianoHomeworkMapper;
import com.ruoyi.piano.mapper.PianoHomeworkSubmissionMapper;
import com.ruoyi.piano.service.IPianoHomeworkSubmissionService;

/**
 * 钢琴作业提交点评Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PianoHomeworkSubmissionServiceImpl implements IPianoHomeworkSubmissionService
{
    @Autowired
    private PianoHomeworkSubmissionMapper pianoHomeworkSubmissionMapper;

    @Autowired
    private PianoHomeworkMapper pianoHomeworkMapper;

    @Override
    public PianoHomeworkSubmission selectPianoHomeworkSubmissionById(Long submissionId)
    {
        return pianoHomeworkSubmissionMapper.selectPianoHomeworkSubmissionById(submissionId);
    }

    @Override
    public List<PianoHomeworkSubmission> selectPianoHomeworkSubmissionList(PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        return pianoHomeworkSubmissionMapper.selectPianoHomeworkSubmissionList(pianoHomeworkSubmission);
    }

    @Override
    public List<PianoHomeworkSubmission> selectPianoHomeworkSubmissionHistory(Long homeworkId)
    {
        return pianoHomeworkSubmissionMapper.selectPianoHomeworkSubmissionHistory(homeworkId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPianoHomeworkSubmission(PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        PianoHomework homework = pianoHomeworkMapper.selectPianoHomeworkById(pianoHomeworkSubmission.getHomeworkId());
        if (homework == null)
        {
            throw new ServiceException("作业不存在或已删除");
        }

        Date submitTime = pianoHomeworkSubmission.getSubmitTime();
        if (submitTime == null)
        {
            submitTime = new Date();
            pianoHomeworkSubmission.setSubmitTime(submitTime);
        }

        Integer maxAttemptNo = pianoHomeworkSubmissionMapper.selectMaxAttemptNoByHomeworkId(pianoHomeworkSubmission.getHomeworkId());
        int attemptNo = maxAttemptNo == null ? 1 : maxAttemptNo + 1;
        pianoHomeworkSubmission.setAttemptNo(attemptNo);
        pianoHomeworkSubmission.setStudentId(homework.getStudentId());
        pianoHomeworkSubmission.setSubmitType(attemptNo == 1 ? "0" : "1");
        pianoHomeworkSubmission.setOverdueFlag(isOverdue(homework.getSubmitDeadline(), submitTime) ? "1" : "0");
        pianoHomeworkSubmission.setReviewStatus("0");
        pianoHomeworkSubmission.setCorrectionRequired("0");
        pianoHomeworkSubmission.setLatestFlag("1");

        pianoHomeworkSubmissionMapper.clearLatestFlagByHomeworkId(pianoHomeworkSubmission.getHomeworkId());
        int rows = pianoHomeworkSubmissionMapper.insertPianoHomeworkSubmission(pianoHomeworkSubmission);
        pianoHomeworkMapper.updatePianoHomeworkStatus(pianoHomeworkSubmission.getHomeworkId(), "1");
        return rows;
    }

    @Override
    public int updatePianoHomeworkSubmission(PianoHomeworkSubmission pianoHomeworkSubmission)
    {
        return pianoHomeworkSubmissionMapper.updatePianoHomeworkSubmission(pianoHomeworkSubmission);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reviewPianoHomeworkSubmission(PianoHomeworkReviewRequest reviewRequest)
    {
        PianoHomeworkSubmission submission = pianoHomeworkSubmissionMapper.selectPianoHomeworkSubmissionById(reviewRequest.getSubmissionId());
        if (submission == null)
        {
            throw new ServiceException("提交记录不存在");
        }
        if (!StringUtils.equals("1", submission.getLatestFlag()))
        {
            throw new ServiceException("只能点评最新提交记录");
        }

        PianoHomeworkSubmission review = new PianoHomeworkSubmission();
        review.setSubmissionId(reviewRequest.getSubmissionId());
        review.setScore(reviewRequest.getScore());
        review.setReviewContent(reviewRequest.getReviewContent());
        review.setCorrectionRequired(reviewRequest.getCorrectionRequired());
        review.setReviewStatus("1");
        review.setReviewTime(new Date());
        int rows = pianoHomeworkSubmissionMapper.updatePianoHomeworkSubmissionReview(review);

        String nextHomeworkStatus = StringUtils.equals("1", reviewRequest.getCorrectionRequired()) ? "2" : "3";
        pianoHomeworkMapper.updatePianoHomeworkStatus(submission.getHomeworkId(), nextHomeworkStatus);
        return rows;
    }

    @Override
    public int deletePianoHomeworkSubmissionByIds(Long[] submissionIds)
    {
        return pianoHomeworkSubmissionMapper.deletePianoHomeworkSubmissionByIds(submissionIds);
    }

    @Override
    public int deletePianoHomeworkSubmissionById(Long submissionId)
    {
        return pianoHomeworkSubmissionMapper.deletePianoHomeworkSubmissionById(submissionId);
    }

    /**
     * 判断提交时间是否晚于老师设置的截止时间。
     *
     * @param submitDeadline 提交截止时间
     * @param submitTime 实际提交时间
     * @return true 表示逾期提交
     */
    private boolean isOverdue(Date submitDeadline, Date submitTime)
    {
        return submitDeadline != null && submitTime != null && submitTime.after(submitDeadline);
    }
}
