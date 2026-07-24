package com.ruoyi.piano.mapper;

import java.util.List;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;

/**
 * 钢琴作业提交点评Mapper接口
 *
 * @author ruoyi
 */
public interface PianoHomeworkSubmissionMapper
{
    public PianoHomeworkSubmission selectPianoHomeworkSubmissionById(Long submissionId);

    public List<PianoHomeworkSubmission> selectPianoHomeworkSubmissionList(PianoHomeworkSubmission pianoHomeworkSubmission);

    public List<PianoHomeworkSubmission> selectPianoHomeworkSubmissionHistory(Long homeworkId);

    public List<PianoHomeworkSubmission> selectDashboardRecentSubmissions();

    public Integer selectMaxAttemptNoByHomeworkId(Long homeworkId);

    public int clearLatestFlagByHomeworkId(Long homeworkId);

    public int insertPianoHomeworkSubmission(PianoHomeworkSubmission pianoHomeworkSubmission);

    public int updatePianoHomeworkSubmission(PianoHomeworkSubmission pianoHomeworkSubmission);

    public int updatePianoHomeworkSubmissionReview(PianoHomeworkSubmission pianoHomeworkSubmission);

    public int deletePianoHomeworkSubmissionById(Long submissionId);

    public int deletePianoHomeworkSubmissionByIds(Long[] submissionIds);
}
