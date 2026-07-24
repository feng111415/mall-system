package com.ruoyi.piano.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 钢琴作业点评请求对象
 *
 * @author ruoyi
 */
public class PianoHomeworkReviewRequest
{
    /** 提交ID */
    @NotNull(message = "提交记录不能为空")
    private Long submissionId;

    /** 老师评分 */
    @Min(value = 0, message = "老师评分不能小于0")
    @Max(value = 100, message = "老师评分不能大于100")
    private Integer score;

    /** 老师点评 */
    @Size(max = 1000, message = "老师点评不能超过1000个字符")
    private String reviewContent;

    /** 是否需要订正（0否 1是） */
    @NotNull(message = "是否需要订正不能为空")
    @Pattern(regexp = "^[01]$", message = "是否需要订正只能为0或1")
    private String correctionRequired;

    public Long getSubmissionId()
    {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId)
    {
        this.submissionId = submissionId;
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
}
