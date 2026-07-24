package com.ruoyi.piano.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 钢琴作业提交点评对象 piano_homework_submission
 *
 * @author ruoyi
 */
public class PianoHomeworkSubmission extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 提交ID */
    private Long submissionId;

    /** 作业ID */
    @NotNull(message = "作业不能为空")
    private Long homeworkId;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名，仅用于列表展示 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 作业标题，仅用于列表展示 */
    @Excel(name = "作业标题")
    private String homeworkTitle;

    /** 提交截止时间，仅用于列表展示 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitDeadline;

    /** 第几次提交 */
    @Excel(name = "第几次提交")
    private Integer attemptNo;

    /** 提交类型（0首次提交 1订正提交） */
    @Excel(name = "提交类型", readConverterExp = "0=首次提交,1=订正提交")
    private String submitType;

    /** 学生提交说明 */
    @Excel(name = "学生提交说明")
    @Size(max = 1000, message = "学生提交说明不能超过1000个字符")
    private String submitContent;

    /** 附件地址 */
    @Excel(name = "附件地址")
    @Size(max = 500, message = "附件地址不能超过500个字符")
    private String attachmentUrl;

    /** 提交时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitTime;

    /** 是否逾期提交（0否 1是） */
    @Excel(name = "是否逾期提交", readConverterExp = "0=否,1=是")
    private String overdueFlag;

    /** 点评状态（0待点评 1已点评） */
    @Excel(name = "点评状态", readConverterExp = "0=待点评,1=已点评")
    private String reviewStatus;

    /** 老师评分 */
    @Excel(name = "老师评分")
    @Min(value = 0, message = "老师评分不能小于0")
    @Max(value = 100, message = "老师评分不能大于100")
    private Integer score;

    /** 老师点评 */
    @Excel(name = "老师点评")
    @Size(max = 1000, message = "老师点评不能超过1000个字符")
    private String reviewContent;

    /** 是否需要订正（0否 1是） */
    @Excel(name = "是否需要订正", readConverterExp = "0=否,1=是")
    @Pattern(regexp = "^[01]$", message = "是否需要订正只能为0或1")
    private String correctionRequired;

    /** 点评时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "点评时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date reviewTime;

    /** 是否最新提交（0否 1是） */
    @Excel(name = "是否最新提交", readConverterExp = "0=否,1=是")
    private String latestFlag;

    public Long getSubmissionId()
    {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId)
    {
        this.submissionId = submissionId;
    }

    public Long getHomeworkId()
    {
        return homeworkId;
    }

    public void setHomeworkId(Long homeworkId)
    {
        this.homeworkId = homeworkId;
    }

    public Long getStudentId()
    {
        return studentId;
    }

    public void setStudentId(Long studentId)
    {
        this.studentId = studentId;
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

    public Date getSubmitDeadline()
    {
        return submitDeadline;
    }

    public void setSubmitDeadline(Date submitDeadline)
    {
        this.submitDeadline = submitDeadline;
    }

    public Integer getAttemptNo()
    {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo)
    {
        this.attemptNo = attemptNo;
    }

    public String getSubmitType()
    {
        return submitType;
    }

    public void setSubmitType(String submitType)
    {
        this.submitType = submitType;
    }

    public String getSubmitContent()
    {
        return submitContent;
    }

    public void setSubmitContent(String submitContent)
    {
        this.submitContent = submitContent;
    }

    public String getAttachmentUrl()
    {
        return attachmentUrl;
    }

    public void setAttachmentUrl(String attachmentUrl)
    {
        this.attachmentUrl = attachmentUrl;
    }

    public Date getSubmitTime()
    {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime)
    {
        this.submitTime = submitTime;
    }

    public String getOverdueFlag()
    {
        return overdueFlag;
    }

    public void setOverdueFlag(String overdueFlag)
    {
        this.overdueFlag = overdueFlag;
    }

    public String getReviewStatus()
    {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus)
    {
        this.reviewStatus = reviewStatus;
    }

    public Integer getScore()
    {
        return score;
    }

    public void setScore(Integer score)
    {
        this.score = score;
    }

    public String getReviewContent()
    {
        return reviewContent;
    }

    public void setReviewContent(String reviewContent)
    {
        this.reviewContent = reviewContent;
    }

    public String getCorrectionRequired()
    {
        return correctionRequired;
    }

    public void setCorrectionRequired(String correctionRequired)
    {
        this.correctionRequired = correctionRequired;
    }

    public Date getReviewTime()
    {
        return reviewTime;
    }

    public void setReviewTime(Date reviewTime)
    {
        this.reviewTime = reviewTime;
    }

    public String getLatestFlag()
    {
        return latestFlag;
    }

    public void setLatestFlag(String latestFlag)
    {
        this.latestFlag = latestFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("submissionId", getSubmissionId())
            .append("homeworkId", getHomeworkId())
            .append("studentId", getStudentId())
            .append("studentName", getStudentName())
            .append("homeworkTitle", getHomeworkTitle())
            .append("submitDeadline", getSubmitDeadline())
            .append("attemptNo", getAttemptNo())
            .append("submitType", getSubmitType())
            .append("submitContent", getSubmitContent())
            .append("attachmentUrl", getAttachmentUrl())
            .append("submitTime", getSubmitTime())
            .append("overdueFlag", getOverdueFlag())
            .append("reviewStatus", getReviewStatus())
            .append("score", getScore())
            .append("reviewContent", getReviewContent())
            .append("correctionRequired", getCorrectionRequired())
            .append("reviewTime", getReviewTime())
            .append("latestFlag", getLatestFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
