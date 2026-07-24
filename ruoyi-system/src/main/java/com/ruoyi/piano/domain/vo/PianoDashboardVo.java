package com.ruoyi.piano.domain.vo;

import java.io.Serializable;
import java.util.List;
import com.ruoyi.piano.domain.PianoHomework;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;

/**
 * 钢琴作业今日工作台展示对象
 *
 * @author ruoyi
 */
public class PianoDashboardVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 在学学生数 */
    private Long studentCount;

    /** 本周布置作业数 */
    private Long weekHomeworkCount;

    /** 待点评作业数 */
    private Long pendingReviewCount;

    /** 需订正作业数 */
    private Long correctionCount;

    /** 逾期未完成作业数 */
    private Long overdueCount;

    /** 最近待提交作业 */
    private List<PianoHomework> todoHomeworks;

    /** 最近提交动态 */
    private List<PianoHomeworkSubmission> recentSubmissions;

    public Long getStudentCount()
    {
        return studentCount;
    }

    public void setStudentCount(Long studentCount)
    {
        this.studentCount = studentCount;
    }

    public Long getWeekHomeworkCount()
    {
        return weekHomeworkCount;
    }

    public void setWeekHomeworkCount(Long weekHomeworkCount)
    {
        this.weekHomeworkCount = weekHomeworkCount;
    }

    public Long getPendingReviewCount()
    {
        return pendingReviewCount;
    }

    public void setPendingReviewCount(Long pendingReviewCount)
    {
        this.pendingReviewCount = pendingReviewCount;
    }

    public Long getCorrectionCount()
    {
        return correctionCount;
    }

    public void setCorrectionCount(Long correctionCount)
    {
        this.correctionCount = correctionCount;
    }

    public Long getOverdueCount()
    {
        return overdueCount;
    }

    public void setOverdueCount(Long overdueCount)
    {
        this.overdueCount = overdueCount;
    }

    public List<PianoHomework> getTodoHomeworks()
    {
        return todoHomeworks;
    }

    public void setTodoHomeworks(List<PianoHomework> todoHomeworks)
    {
        this.todoHomeworks = todoHomeworks;
    }

    public List<PianoHomeworkSubmission> getRecentSubmissions()
    {
        return recentSubmissions;
    }

    public void setRecentSubmissions(List<PianoHomeworkSubmission> recentSubmissions)
    {
        this.recentSubmissions = recentSubmissions;
    }
}
