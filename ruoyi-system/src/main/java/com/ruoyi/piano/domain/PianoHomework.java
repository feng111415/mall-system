package com.ruoyi.piano.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 钢琴课后作业对象 piano_homework
 *
 * @author ruoyi
 */
public class PianoHomework extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 作业ID */
    private Long homeworkId;

    /** 学生端提交码 */
    @Excel(name = "学生端提交码")
    @Size(max = 16, message = "学生端提交码不能超过16个字符")
    private String submitCode;

    /** 学生ID */
    @NotNull(message = "学生不能为空")
    private Long studentId;

    /** 学生姓名，仅用于列表展示 */
    @Excel(name = "学生姓名")
    private String studentName;

    /** 作业标题 */
    @Excel(name = "作业标题")
    @NotBlank(message = "作业标题不能为空")
    @Size(max = 100, message = "作业标题不能超过100个字符")
    private String homeworkTitle;

    /** 练习内容 */
    @Excel(name = "练习内容")
    @NotBlank(message = "练习内容不能为空")
    private String practiceContent;

    /** 练习要求 */
    @Excel(name = "练习要求")
    @Size(max = 1000, message = "练习要求不能超过1000个字符")
    private String practiceRequirement;

    /** 提交截止时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "提交截止时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date submitDeadline;

    /** 作业状态（0待提交 1已提交待点评 2需订正 3已完成 4已逾期） */
    @Excel(name = "作业状态", readConverterExp = "0=待提交,1=已提交待点评,2=需订正,3=已完成,4=已逾期")
    @Pattern(regexp = "^[0-4]$", message = "作业状态只能为0、1、2、3、4")
    private String homeworkStatus;

    /** 老师备注 */
    @Excel(name = "老师备注")
    @Size(max = 500, message = "老师备注不能超过500个字符")
    private String teacherRemark;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

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

    public String getTeacherRemark()
    {
        return teacherRemark;
    }

    public void setTeacherRemark(String teacherRemark)
    {
        this.teacherRemark = teacherRemark;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("homeworkId", getHomeworkId())
            .append("submitCode", getSubmitCode())
            .append("studentId", getStudentId())
            .append("studentName", getStudentName())
            .append("homeworkTitle", getHomeworkTitle())
            .append("practiceContent", getPracticeContent())
            .append("practiceRequirement", getPracticeRequirement())
            .append("submitDeadline", getSubmitDeadline())
            .append("homeworkStatus", getHomeworkStatus())
            .append("teacherRemark", getTeacherRemark())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
