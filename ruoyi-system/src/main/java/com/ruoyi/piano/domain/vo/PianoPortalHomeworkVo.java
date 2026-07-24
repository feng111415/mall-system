package com.ruoyi.piano.domain.vo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.piano.domain.PianoHomeworkSubmission;

/**
 * 学生端作业详情展示对象
 *
 * @author ruoyi
 */
public class PianoPortalHomeworkVo implements Serializable
{
    private static final long serialVersionUID = 1L;

    private Long homeworkId;

    private String submitCode;

    private String studentName;

    private String homeworkTitle;

    private String practiceContent;

    private String practiceRequirement;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date submitDeadline;

    private String homeworkStatus;

    private PianoHomeworkSubmission latestSubmission;

    private List<PianoHomeworkSubmission> submissionHistory;

    public Long getHomeworkId()
    {
        return homeworkId;
    }

    public void setHomeworkId(Long homeworkId)
    {
        this.homeworkId = homeworkId;
    }

    public String getSubmitCode()
    {
        return submitCode;
    }

    public void setSubmitCode(String submitCode)
    {
        this.submitCode = submitCode;
    }

    public String getStudentName()
    {
        return studentName;
    }

    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public String getHomeworkTitle()
    {
        return homeworkTitle;
    }

    public void setHomeworkTitle(String homeworkTitle)
    {
        this.homeworkTitle = homeworkTitle;
    }

    public String getPracticeContent()
    {
        return practiceContent;
    }

    public void setPracticeContent(String practiceContent)
    {
        this.practiceContent = practiceContent;
    }

    public String getPracticeRequirement()
    {
        return practiceRequirement;
    }

    public void setPracticeRequirement(String practiceRequirement)
    {
        this.practiceRequirement = practiceRequirement;
    }

    public Date getSubmitDeadline()
    {
        return submitDeadline;
    }

    public void setSubmitDeadline(Date submitDeadline)
    {
        this.submitDeadline = submitDeadline;
    }

    public String getHomeworkStatus()
    {
        return homeworkStatus;
    }

    public void setHomeworkStatus(String homeworkStatus)
    {
        this.homeworkStatus = homeworkStatus;
    }

    public PianoHomeworkSubmission getLatestSubmission()
    {
        return latestSubmission;
    }

    public void setLatestSubmission(PianoHomeworkSubmission latestSubmission)
    {
        this.latestSubmission = latestSubmission;
    }

    public List<PianoHomeworkSubmission> getSubmissionHistory()
    {
        return submissionHistory;
    }

    public void setSubmissionHistory(List<PianoHomeworkSubmission> submissionHistory)
    {
        this.submissionHistory = submissionHistory;
    }
}
