package com.ruoyi.mall.member.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberDevice;
import com.ruoyi.mall.member.domain.MallMemberDeviceAudit;
import com.ruoyi.mall.member.domain.MallMemberSession;
import com.ruoyi.mall.member.domain.vo.MallMemberSessionOverviewVo;
import com.ruoyi.mall.member.domain.vo.MallMemberSessionVo;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.mapper.MallMemberSessionMapper;
import com.ruoyi.mall.member.service.MallDeviceClassifier.DeviceInfo;
import com.ruoyi.mall.member.service.MallMemberSessionPolicy.LoginDecision;
import com.ruoyi.mall.member.service.MallMemberSessionPolicy.OnlineSession;
import com.ruoyi.mall.member.service.MallMemberTokenService.MemberSession;
import com.ruoyi.mall.member.service.MallMemberTokenService.TokenValue;

@Service
public class MallMemberSessionService
{
    private static final int SESSION_DAYS = 7;
    private static final int PRIMARY_CHANGE_DAYS = 30;
    private static final int MAX_PRIMARY_CHANGES = 3;
    private final MallMemberMapper memberMapper;
    private final MallMemberSessionMapper sessionMapper;
    private final MallMemberSessionPolicy sessionPolicy;
    private final MallDeviceClassifier deviceClassifier;
    private final MallMemberTokenService tokenService;
    private final MemberMessagePort memberMessagePort;

    public MallMemberSessionService(MallMemberMapper memberMapper, MallMemberSessionMapper sessionMapper,
            MallMemberSessionPolicy sessionPolicy, MallDeviceClassifier deviceClassifier,
            MallMemberTokenService tokenService)
    { this(memberMapper, sessionMapper, sessionPolicy, deviceClassifier, tokenService, null); }

    @Autowired
    public MallMemberSessionService(MallMemberMapper memberMapper, MallMemberSessionMapper sessionMapper,
            MallMemberSessionPolicy sessionPolicy, MallDeviceClassifier deviceClassifier,
            MallMemberTokenService tokenService, MemberMessagePort memberMessagePort)
    {
        this.memberMapper = memberMapper;
        this.sessionMapper = sessionMapper;
        this.sessionPolicy = sessionPolicy;
        this.deviceClassifier = deviceClassifier;
        this.tokenService = tokenService;
        this.memberMessagePort = memberMessagePort;
    }

    @Transactional
    public LoginSession login(Long memberId, String deviceId, String userAgent, String requestIp)
    {
        requireActiveMemberForUpdate(memberId);
        DeviceInfo detected = deviceClassifier.classify(userAgent);
        MallMemberDevice device = sessionMapper.selectDevice(memberId, deviceId);
        if (device == null)
        {
            device = new MallMemberDevice();
            device.setMemberId(memberId);
            device.setDeviceIdentifier(deviceId);
            device.setDeviceType(detected.type());
            device.setDeviceName(detected.name());
            device.setPrimaryMobile("MOBILE".equals(detected.type())
                    && sessionMapper.selectPrimaryMobileDevice(memberId) == null ? "1" : "0");
            device.setLastLoginIp(requestIp);
            if (sessionMapper.insertDevice(device) != 1) throw new ServiceException("设备登记失败，请稍后重试");
        }
        else
        {
            String deviceName = device.getDeviceType().equals(detected.type())
                    ? detected.name() : device.getDeviceName();
            detected = new DeviceInfo(device.getDeviceType(), deviceName);
            device.setDeviceName(detected.name());
            device.setLastLoginIp(requestIp);
            if (sessionMapper.updateDeviceLogin(device) != 1) throw new ServiceException("设备信息更新失败，请稍后重试");
        }

        LocalDateTime now = LocalDateTime.now();
        sessionMapper.expireSessions(memberId, now);
        List<MallMemberSession> active = sessionMapper.selectActiveSessionsForUpdate(memberId, now);
        List<OnlineSession> online = active.stream()
                .map(value -> new OnlineSession(value.getSessionId(), value.getDeviceIdentifier(),
                        value.getDeviceType(), "1".equals(value.getPrimaryMobile())))
                .toList();
        LoginDecision decision = sessionPolicy.decideLogin(deviceId, detected.type(),
                "1".equals(device.getPrimaryMobile()), online);

        List<String> revokedTokenHashes = new ArrayList<>();
        for (MallMemberSession value : active)
        {
            if (!decision.replacedSessionIds().contains(value.getSessionId())) continue;
            String reason = deviceId.equals(value.getDeviceIdentifier()) ? "SAME_DEVICE_LOGIN"
                    : "DESKTOP".equals(detected.type()) ? "DESKTOP_REPLACED" : "MOBILE_LIMIT";
            if (sessionMapper.markSessionOffline(value.getSessionId(), "REPLACED", reason) != 1)
                throw new ServiceException("旧会话下线失败，请稍后重试");
            revokedTokenHashes.add(value.getTokenHash());
        }

        TokenValue token = tokenService.issueToken();
        MallMemberSession newSession = new MallMemberSession();
        newSession.setMemberId(memberId);
        newSession.setMemberDeviceId(device.getMemberDeviceId());
        newSession.setTokenHash(token.hash());
        newSession.setLoginIp(requestIp);
        newSession.setExpireTime(now.plusDays(SESSION_DAYS));
        if (sessionMapper.insertSession(newSession) != 1) throw new ServiceException("会话创建失败，请稍后重试");

        MemberSession cacheSession = cacheSession(newSession, device, token.hash());
        if (memberMessagePort != null)
            memberMessagePort.publish(memberId, "ACCOUNT", "新设备登录",
                    detected.name() + " 已登录你的商城账号", "如果这不是你的操作，请立即在安全中心下线该设备",
                    "SESSION", newSession.getSessionId(), null, "/account");
        afterCommit(() -> {
            revokedTokenHashes.forEach(tokenService::deleteByHash);
            tokenService.activateToken(token, cacheSession);
        });
        return new LoginSession(token.raw(), SESSION_DAYS * 24 * 60 * 60, newSession.getSessionId());
    }

    public MallMemberSessionOverviewVo overview(MemberSession current)
    {
        LocalDateTime now = LocalDateTime.now();
        List<MallMemberSessionVo> sessions = sessionMapper.selectActiveSessions(current.getMemberId(), now).stream()
                .map(value -> MallMemberSessionVo.from(value, current.getSessionId()))
                .toList();
        int used = sessionMapper.countPrimaryChangesSince(current.getMemberId(), now.minusDays(PRIMARY_CHANGE_DAYS));
        return new MallMemberSessionOverviewVo(sessions, Math.max(0, MAX_PRIMARY_CHANGES - used), PRIMARY_CHANGE_DAYS);
    }

    @Transactional
    public void logout(MemberSession current)
    {
        sessionMapper.markSessionOffline(current.getSessionId(), "LOGGED_OUT", "MEMBER_LOGOUT");
        afterCommit(() -> tokenService.deleteByHash(current.getTokenHash()));
    }

    @Transactional
    public MallMemberSessionOverviewVo revoke(MemberSession current, Long targetSessionId)
    {
        if (targetSessionId == null || targetSessionId <= 0) throw new ServiceException("会话参数无效");
        if (targetSessionId.equals(current.getSessionId())) throw new ServiceException("当前设备请使用退出登录");
        requireActiveMemberForUpdate(current.getMemberId());
        MallMemberSession target = sessionMapper.selectActiveSession(current.getMemberId(), targetSessionId, LocalDateTime.now());
        if (target == null) throw new ServiceException("在线会话不存在");
        if (sessionMapper.markSessionOffline(targetSessionId, "REVOKED", "MEMBER_REVOKE") != 1)
            throw new ServiceException("设备下线失败，请稍后重试");
        afterCommit(() -> tokenService.deleteByHash(target.getTokenHash()));
        return overview(current);
    }

    public void assertPrimaryChangeAllowed(MemberSession current)
    {
        MallMemberDevice currentDevice = sessionMapper.selectDeviceById(current.getMemberId(), current.getMemberDeviceId());
        if (currentDevice == null || !"MOBILE".equals(currentDevice.getDeviceType()))
            throw new ServiceException("只有移动设备可以设为主设备");
        if ("1".equals(currentDevice.getPrimaryMobile())) throw new ServiceException("当前设备已经是主移动设备");
        int used = sessionMapper.countPrimaryChangesSince(current.getMemberId(),
                LocalDateTime.now().minusDays(PRIMARY_CHANGE_DAYS));
        if (used >= MAX_PRIMARY_CHANGES) throw new ServiceException("主设备 30 天内最多更换 3 次");
    }

    @Transactional
    public MallMemberSessionOverviewVo replacePrimary(MemberSession current, String requestIp)
    {
        requireActiveMemberForUpdate(current.getMemberId());
        assertPrimaryChangeAllowed(current);
        MallMemberDevice oldPrimary = sessionMapper.selectPrimaryMobileDevice(current.getMemberId());
        sessionMapper.clearPrimaryMobile(current.getMemberId());
        if (sessionMapper.markPrimaryMobile(current.getMemberId(), current.getMemberDeviceId()) != 1)
            throw new ServiceException("主设备更换失败，请稍后重试");
        MallMemberDeviceAudit audit = new MallMemberDeviceAudit();
        audit.setMemberId(current.getMemberId());
        audit.setActionType("PRIMARY_MOBILE_REPLACED");
        audit.setOldMemberDeviceId(oldPrimary == null ? null : oldPrimary.getMemberDeviceId());
        audit.setNewMemberDeviceId(current.getMemberDeviceId());
        audit.setRequestIp(requestIp);
        if (sessionMapper.insertDeviceAudit(audit) != 1) throw new ServiceException("主设备审计记录失败");
        if (memberMessagePort != null)
            memberMessagePort.publish(current.getMemberId(), "ACCOUNT", "主设备已更换",
                    "当前设备已设为主移动设备", "如非本人操作，请检查登录设备并及时下线异常设备",
                    "DEVICE", current.getMemberDeviceId(), null, "/account");
        List<MallMemberSession> active = sessionMapper.selectActiveSessions(current.getMemberId(), LocalDateTime.now());
        afterCommit(() -> active.forEach(value -> tokenService.updatePrimaryMobile(
                value.getTokenHash(), "1".equals(value.getPrimaryMobile()))));
        return overview(current);
    }

    private MemberSession cacheSession(MallMemberSession session, MallMemberDevice device, String tokenHash)
    {
        MemberSession value = new MemberSession();
        value.setMemberId(session.getMemberId());
        value.setSessionId(session.getSessionId());
        value.setMemberDeviceId(device.getMemberDeviceId());
        value.setDeviceId(device.getDeviceIdentifier());
        value.setDeviceType(device.getDeviceType());
        value.setPrimaryMobile("1".equals(device.getPrimaryMobile()));
        value.setTokenHash(tokenHash);
        return value;
    }

    private void requireActiveMemberForUpdate(Long memberId)
    {
        MallMember member = memberMapper.selectByIdForUpdate(memberId);
        if (member == null || !"0".equals(member.getStatus())) throw new ServiceException("会员不存在或已停用");
    }

    private void afterCommit(Runnable action)
    {
        if (!TransactionSynchronizationManager.isSynchronizationActive())
        {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization()
        {
            @Override
            public void afterCommit() { action.run(); }
        });
    }

    public record LoginSession(String token, int expiresIn, Long sessionId)
    {
    }
}
