package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MallCaptchaVerifyRequest
{
    @NotBlank(message = "挑战编号不能为空")
    private String challengeId;

    @NotNull(message = "请完成拼图")
    @Min(value = 0, message = "验证位置无效")
    @Max(value = 1000, message = "验证位置无效")
    private Integer position;

    public String getChallengeId() { return challengeId; }
    public void setChallengeId(String value) { challengeId = value; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer value) { position = value; }
}
