package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MallPhoneChangeStartRequest
{
    @NotBlank
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String newPhone;

    public String getNewPhone() { return newPhone; }
    public void setNewPhone(String value) { newPhone = value; }
}
