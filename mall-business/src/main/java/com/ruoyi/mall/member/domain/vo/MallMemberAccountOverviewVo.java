package com.ruoyi.mall.member.domain.vo;

import java.util.Date;
import java.util.List;
import com.ruoyi.mall.member.domain.MallMemberConsent;

public class MallMemberAccountOverviewVo
{
    private final Long memberId;
    private final String nickname;
    private final String avatar;
    private final String maskedPhone;
    private final String status;
    private final Date registerTime;
    private final Date lastLoginTime;
    private final int onlineDeviceCount;
    private final List<MallMemberConsent> consents;
    private final List<MallMemberLifecycleEventVo> lifecycle;
    private final MallCancellationEligibilityVo cancellationEligibility;

    public MallMemberAccountOverviewVo(Long memberId, String nickname, String avatar, String maskedPhone,
            String status, Date registerTime, Date lastLoginTime, int onlineDeviceCount,
            List<MallMemberConsent> consents, List<MallMemberLifecycleEventVo> lifecycle,
            MallCancellationEligibilityVo cancellationEligibility)
    {
        this.memberId = memberId;
        this.nickname = nickname;
        this.avatar = avatar;
        this.maskedPhone = maskedPhone;
        this.status = status;
        this.registerTime = registerTime;
        this.lastLoginTime = lastLoginTime;
        this.onlineDeviceCount = onlineDeviceCount;
        this.consents = consents;
        this.lifecycle = lifecycle;
        this.cancellationEligibility = cancellationEligibility;
    }

    public Long getMemberId() { return memberId; }
    public String getNickname() { return nickname; }
    public String getAvatar() { return avatar; }
    public String getMaskedPhone() { return maskedPhone; }
    public String getStatus() { return status; }
    public Date getRegisterTime() { return registerTime; }
    public Date getLastLoginTime() { return lastLoginTime; }
    public int getOnlineDeviceCount() { return onlineDeviceCount; }
    public List<MallMemberConsent> getConsents() { return consents; }
    public List<MallMemberLifecycleEventVo> getLifecycle() { return lifecycle; }
    public MallCancellationEligibilityVo getCancellationEligibility() { return cancellationEligibility; }
}
