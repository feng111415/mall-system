package com.ruoyi.mall.member.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.mall.member.domain.MallMember;

public class MallMemberProfileVo
{
    private Long memberId;
    private String maskedPhone;
    private String nickname;
    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastLoginTime;

    public static MallMemberProfileVo from(MallMember member)
    {
        MallMemberProfileVo profile = new MallMemberProfileVo();
        profile.memberId = member.getMemberId();
        profile.maskedPhone = maskPhone(member.getPhone());
        profile.nickname = member.getNickname();
        profile.avatar = member.getAvatar();
        profile.lastLoginTime = member.getLastLoginTime();
        return profile;
    }

    private static String maskPhone(String phone)
    {
        return phone == null || phone.length() != 11 ? "" : phone.substring(0, 3) + "****" + phone.substring(7);
    }

    public Long getMemberId() { return memberId; }
    public String getMaskedPhone() { return maskedPhone; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }
    public Date getLastLoginTime() { return lastLoginTime; }
}
