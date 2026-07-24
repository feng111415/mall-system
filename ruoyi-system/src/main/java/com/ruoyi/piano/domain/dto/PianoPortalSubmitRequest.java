package com.ruoyi.piano.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 学生端作业提交请求对象
 *
 * @author ruoyi
 */
public class PianoPortalSubmitRequest
{
    /** 学生端提交码 */
    @NotBlank(message = "提交码不能为空")
    @Size(max = 16, message = "提交码不能超过16个字符")
    private String submitCode;

    /** 学生提交说明 */
    @Size(max = 1000, message = "提交说明不能超过1000个字符")
    private String submitContent;

    /** 附件地址 */
    @Size(max = 500, message = "附件地址不能超过500个字符")
    private String attachmentUrl;

    public String getSubmitCode()
    {
        return submitCode;
    }

    public void setSubmitCode(String submitCode)
    {
        this.submitCode = submitCode;
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
}
