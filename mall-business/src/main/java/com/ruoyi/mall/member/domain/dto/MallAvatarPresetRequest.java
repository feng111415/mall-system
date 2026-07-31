package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MallAvatarPresetRequest
{
    @NotBlank(message = "请选择预设头像")
    @Pattern(regexp = "^[a-z]{2,20}$", message = "预设头像代码格式不正确")
    private String presetCode;

    public String getPresetCode() { return presetCode; }
    public void setPresetCode(String presetCode) { this.presetCode = presetCode; }
}
