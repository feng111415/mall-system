package com.ruoyi.mall.member.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.ruoyi.common.exception.ServiceException;

@Component
public class MallMemberSessionPolicy
{
    private static final int MAX_MOBILE_SESSIONS = 2;

    public LoginDecision decideLogin(String deviceId, String deviceType, boolean currentDevicePrimary,
            List<OnlineSession> onlineSessions)
    {
        List<Long> replaced = new ArrayList<>();
        List<OnlineSession> remaining = new ArrayList<>();
        for (OnlineSession session : onlineSessions)
        {
            if (deviceId.equals(session.deviceId()))
            {
                replaced.add(session.sessionId());
            }
            else
            {
                remaining.add(session);
            }
        }

        if ("DESKTOP".equals(deviceType))
        {
            remaining.stream()
                    .filter(session -> "DESKTOP".equals(session.deviceType()))
                    .map(OnlineSession::sessionId)
                    .forEach(replaced::add);
            return new LoginDecision(false, List.copyOf(replaced));
        }
        if (!"MOBILE".equals(deviceType))
        {
            throw new ServiceException("设备类型无效");
        }

        List<OnlineSession> mobileSessions = remaining.stream()
                .filter(session -> "MOBILE".equals(session.deviceType()))
                .toList();
        if (mobileSessions.size() >= MAX_MOBILE_SESSIONS)
        {
            OnlineSession ordinary = mobileSessions.stream()
                    .filter(session -> !session.primaryMobile())
                    .findFirst()
                    .orElseThrow(() -> new ServiceException("移动设备会话状态异常，请稍后重试"));
            replaced.add(ordinary.sessionId());
        }
        return new LoginDecision(currentDevicePrimary, List.copyOf(replaced));
    }

    public record OnlineSession(Long sessionId, String deviceId, String deviceType, boolean primaryMobile)
    {
    }

    public record LoginDecision(boolean primaryMobile, List<Long> replacedSessionIds)
    {
    }
}
