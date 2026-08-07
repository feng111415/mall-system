package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MallAccountCancellationRequest
{
    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;

    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
}
