package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MallCaptchaChallengeRequest
{
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的中国大陆手机号码")
    private String phone;

    public String getPhone() { return phone; }
    public void setPhone(String value) { phone = value; }
}
