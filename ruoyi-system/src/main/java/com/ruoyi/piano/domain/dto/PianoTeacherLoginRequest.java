package com.ruoyi.piano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 钢琴老师小程序登录请求对象
 *
 * @author ruoyi
 */
public class PianoTeacherLoginRequest
{
    /** 老师端口令 */
    @NotBlank(message = "老师口令不能为空")
    @Size(max = 50, message = "老师口令不能超过50个字符")
    private String teacherCode;

    public String getTeacherCode()
    {
        return teacherCode;
    }

    public void setTeacherCode(String teacherCode)
    {
        this.teacherCode = teacherCode;
    }
}
