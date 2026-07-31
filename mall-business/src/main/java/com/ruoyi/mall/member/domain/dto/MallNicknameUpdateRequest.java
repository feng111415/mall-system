package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class MallNicknameUpdateRequest
{
    @NotBlank(message = "请输入昵称")
    private String nickname;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
