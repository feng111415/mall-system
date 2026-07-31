package com.ruoyi.mall.member.domain.vo;

import java.time.LocalDateTime;
import com.ruoyi.mall.member.domain.MallMemberSession;

public record MallMemberSessionVo(Long sessionId, String deviceType, String deviceName,
        boolean primaryMobile, boolean current, String loginIp, LocalDateTime loginTime,
        LocalDateTime lastActiveTime, LocalDateTime expireTime)
{
    public static MallMemberSessionVo from(MallMemberSession session, Long currentSessionId)
    {
        return new MallMemberSessionVo(session.getSessionId(), session.getDeviceType(), session.getDeviceName(),
                "1".equals(session.getPrimaryMobile()), session.getSessionId().equals(currentSessionId),
                session.getLoginIp(), session.getCreateTime(), session.getLastActiveTime(), session.getExpireTime());
    }
}
