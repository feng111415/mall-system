package com.ruoyi.piano.service;

import java.util.List;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;
import com.ruoyi.piano.domain.dto.PianoHomeworkReviewRequest;

/**
 * 钢琴作业提交点评Service接口
 *
 * @author ruoyi
 */
public interface IPianoHomeworkSubmissionService
{
    public PianoHomeworkSubmission selectPianoHomeworkSubmissionById(Long submissionId);

    public List<PianoHomeworkSubmission> selectPianoHomeworkSubmissionList(PianoHomeworkSubmission pianoHomeworkSubmission);

    public List<PianoHomeworkSubmission> selectPianoHomeworkSubmissionHistory(Long homeworkId);

    public int insertPianoHomeworkSubmission(PianoHomeworkSubmission pianoHomeworkSubmission);

    public int updatePianoHomeworkSubmission(PianoHomeworkSubmission pianoHomeworkSubmission);

    public int reviewPianoHomeworkSubmission(PianoHomeworkReviewRequest reviewRequest);

    public int deletePianoHomeworkSubmissionByIds(Long[] submissionIds);

    public int deletePianoHomeworkSubmissionById(Long submissionId);
}
