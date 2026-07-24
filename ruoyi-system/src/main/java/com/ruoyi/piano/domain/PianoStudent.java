package com.ruoyi.piano.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 钢琴学生档案对象 piano_student
 *
 * @author ruoyi
 */
public class PianoStudent extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 学生ID */
    private Long studentId;

    /** 学生姓名 */
    @Excel(name = "学生姓名")
    @NotBlank(message = "学生姓名不能为空")
    @Size(max = 50, message = "学生姓名不能超过50个字符")
    private String studentName;

    /** 家长姓名 */
    @Excel(name = "家长姓名")
    @Size(max = 50, message = "家长姓名不能超过50个字符")
    private String parentName;

    /** 家长联系电话 */
    @Excel(name = "家长联系电话")
    @Size(max = 20, message = "家长联系电话不能超过20个字符")
    private String parentPhone;

    /** 学习级别 */
    @Excel(name = "学习级别")
    @Size(max = 50, message = "学习级别不能超过50个字符")
    private String levelName;

    /** 学习目标 */
    @Excel(name = "学习目标")
    @Size(max = 200, message = "学习目标不能超过200个字符")
    private String learningGoal;

    /** 状态（0在学 1停用） */
    @Excel(name = "状态", readConverterExp = "0=在学,1=停用")
    @Pattern(regexp = "^[01]$", message = "学生状态只能为0或1")
    private String status;

    /** 删除标志（0存在 2删除） */
    private String delFlag;

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

    public String getParentName()
    {
        return parentName;
    }

    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }

    public String getParentPhone()
    {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone)
    {
        this.parentPhone = parentPhone;
    }

    public String getLevelName()
    {
        return levelName;
    }

    public void setLevelName(String levelName)
    {
        this.levelName = levelName;
    }

    public String getLearningGoal()
    {
        return learningGoal;
    }

    public void setLearningGoal(String learningGoal)
    {
        this.learningGoal = learningGoal;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
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
            .append("studentId", getStudentId())
            .append("studentName", getStudentName())
            .append("parentName", getParentName())
            .append("parentPhone", getParentPhone())
            .append("levelName", getLevelName())
            .append("learningGoal", getLearningGoal())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
