package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberDevice;
import com.ruoyi.mall.member.domain.MallMemberSession;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.mapper.MallMemberSessionMapper;
import com.ruoyi.mall.member.service.MallDeviceClassifier;
import com.ruoyi.mall.member.service.MallMemberSessionPolicy;
import com.ruoyi.mall.member.service.MallMemberSessionService;
import com.ruoyi.mall.member.service.MallMemberTokenService;
import com.ruoyi.mall.member.service.MallMemberTokenService.MemberSession;
import com.ruoyi.mall.member.service.MallMemberTokenService.TokenValue;

class MallMemberSessionServiceTest
{
    private static final Long MEMBER_ID = 7L;
    @Mock private MallMemberMapper memberMapper;
    @Mock private MallMemberSessionMapper sessionMapper;
    @Mock private MallMemberTokenService tokenService;
    private MallMemberSessionService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallMemberSessionService(memberMapper, sessionMapper, new MallMemberSessionPolicy(),
                new MallDeviceClassifier(), tokenService);
        MallMember member = new MallMember();
        member.setMemberId(MEMBER_ID);
        member.setStatus("0");
        when(memberMapper.selectByIdForUpdate(MEMBER_ID)).thenReturn(member);
        when(sessionMapper.expireSessions(eq(MEMBER_ID), any())).thenReturn(0);
        when(sessionMapper.insertDevice(any())).thenAnswer(invocation -> {
            MallMemberDevice value = invocation.getArgument(0);
            value.setMemberDeviceId(100L);
            return 1;
        });
        when(sessionMapper.insertSession(any())).thenAnswer(invocation -> {
            MallMemberSession value = invocation.getArgument(0);
            value.setSessionId(200L);
            return 1;
        });
        when(tokenService.issueToken()).thenReturn(new TokenValue("raw-token", "a".repeat(64)));
    }

    @Test
    void firstMobileIsRegisteredAsPrimary()
    {
        when(sessionMapper.selectActiveSessionsForUpdate(eq(MEMBER_ID), any())).thenReturn(List.of());

        service.login(MEMBER_ID, "1".repeat(32), "Mozilla/5.0 (iPhone) Mobile", "127.0.0.1");

        ArgumentCaptor<MallMemberDevice> device = ArgumentCaptor.forClass(MallMemberDevice.class);
        verify(sessionMapper).insertDevice(device.capture());
        assertEquals("MOBILE", device.getValue().getDeviceType());
        assertEquals("1", device.getValue().getPrimaryMobile());
        verify(tokenService).activateToken(any(), any());
    }

    @Test
    void thirdMobileRevokesOrdinaryMobileAndKeepsPrimary()
    {
        MallMemberDevice device = device(103L, "3".repeat(32), "MOBILE", "0");
        when(sessionMapper.selectDevice(MEMBER_ID, device.getDeviceIdentifier())).thenReturn(device);
        when(sessionMapper.updateDeviceLogin(device)).thenReturn(1);
        MallMemberSession primary = session(11L, 101L, "1".repeat(32), "MOBILE", "1", "b".repeat(64));
        MallMemberSession ordinary = session(12L, 102L, "2".repeat(32), "MOBILE", "0", "c".repeat(64));
        when(sessionMapper.selectActiveSessionsForUpdate(eq(MEMBER_ID), any()))
                .thenReturn(List.of(primary, ordinary));
        when(sessionMapper.markSessionOffline(12L, "REPLACED", "MOBILE_LIMIT")).thenReturn(1);

        service.login(MEMBER_ID, device.getDeviceIdentifier(), "Mozilla/5.0 (Android) Mobile", "127.0.0.1");

        verify(sessionMapper).markSessionOffline(12L, "REPLACED", "MOBILE_LIMIT");
        verify(sessionMapper, never()).markSessionOffline(eq(11L), any(), any());
        verify(tokenService).deleteByHash("c".repeat(64));
    }

    @Test
    void newDesktopRevokesOldDesktopOnly()
    {
        MallMemberDevice device = device(103L, "3".repeat(32), "DESKTOP", "0");
        when(sessionMapper.selectDevice(MEMBER_ID, device.getDeviceIdentifier())).thenReturn(device);
        when(sessionMapper.updateDeviceLogin(device)).thenReturn(1);
        MallMemberSession desktop = session(21L, 101L, "1".repeat(32), "DESKTOP", "0", "d".repeat(64));
        MallMemberSession mobile = session(22L, 102L, "2".repeat(32), "MOBILE", "1", "e".repeat(64));
        when(sessionMapper.selectActiveSessionsForUpdate(eq(MEMBER_ID), any()))
                .thenReturn(List.of(desktop, mobile));
        when(sessionMapper.markSessionOffline(21L, "REPLACED", "DESKTOP_REPLACED")).thenReturn(1);

        service.login(MEMBER_ID, device.getDeviceIdentifier(), "Mozilla/5.0 (Windows NT 10.0)", "127.0.0.1");

        verify(sessionMapper).markSessionOffline(21L, "REPLACED", "DESKTOP_REPLACED");
        verify(sessionMapper, never()).markSessionOffline(eq(22L), any(), any());
    }

    @Test
    void sameDeviceLoginRotatesItsToken()
    {
        String deviceId = "4".repeat(32);
        MallMemberDevice device = device(104L, deviceId, "MOBILE", "0");
        when(sessionMapper.selectDevice(MEMBER_ID, deviceId)).thenReturn(device);
        when(sessionMapper.updateDeviceLogin(device)).thenReturn(1);
        MallMemberSession old = session(31L, 104L, deviceId, "MOBILE", "0", "f".repeat(64));
        when(sessionMapper.selectActiveSessionsForUpdate(eq(MEMBER_ID), any())).thenReturn(List.of(old));
        when(sessionMapper.markSessionOffline(31L, "REPLACED", "SAME_DEVICE_LOGIN")).thenReturn(1);

        service.login(MEMBER_ID, deviceId, "Mozilla/5.0 (iPhone) Mobile", "127.0.0.1");

        verify(sessionMapper).markSessionOffline(31L, "REPLACED", "SAME_DEVICE_LOGIN");
        verify(tokenService).deleteByHash("f".repeat(64));
    }

    @Test
    void existingDeviceCannotChangeCapacityTypeOrNameBySpoofingUserAgent()
    {
        String deviceId = "8".repeat(32);
        MallMemberDevice device = device(108L, deviceId, "MOBILE", "0");
        device.setDeviceName("Android 浏览器");
        when(sessionMapper.selectDevice(MEMBER_ID, deviceId)).thenReturn(device);
        when(sessionMapper.updateDeviceLogin(device)).thenReturn(1);
        when(sessionMapper.selectActiveSessionsForUpdate(eq(MEMBER_ID), any())).thenReturn(List.of());

        service.login(MEMBER_ID, deviceId, "Mozilla/5.0 (Windows NT 10.0)", "127.0.0.1");

        assertEquals("MOBILE", device.getDeviceType());
        assertEquals("Android 浏览器", device.getDeviceName());
    }

    @Test
    void remoteRevokeDeletesTargetToken()
    {
        MemberSession current = currentSession(40L, 140L, false);
        MallMemberSession target = session(41L, 141L, "5".repeat(32), "MOBILE", "0", "1".repeat(64));
        when(sessionMapper.selectActiveSession(eq(MEMBER_ID), eq(41L), any())).thenReturn(target);
        when(sessionMapper.markSessionOffline(41L, "REVOKED", "MEMBER_REVOKE")).thenReturn(1);
        when(sessionMapper.selectActiveSessions(eq(MEMBER_ID), any())).thenReturn(List.of());

        service.revoke(current, 41L);

        verify(tokenService).deleteByHash("1".repeat(64));
    }

    @Test
    void fourthPrimaryChangeWithinThirtyDaysIsRejected()
    {
        MemberSession current = currentSession(50L, 150L, false);
        when(sessionMapper.selectDeviceById(MEMBER_ID, 150L))
                .thenReturn(device(150L, "6".repeat(32), "MOBILE", "0"));
        when(sessionMapper.countPrimaryChangesSince(eq(MEMBER_ID), any())).thenReturn(3);

        assertThrows(ServiceException.class, () -> service.assertPrimaryChangeAllowed(current));
        verify(sessionMapper, never()).markPrimaryMobile(anyLong(), anyLong());
    }

    @Test
    void auditFailureDoesNotUpdateCachedPrimaryFlags()
    {
        MemberSession current = currentSession(60L, 160L, false);
        when(sessionMapper.selectDeviceById(MEMBER_ID, 160L))
                .thenReturn(device(160L, "7".repeat(32), "MOBILE", "0"));
        when(sessionMapper.countPrimaryChangesSince(eq(MEMBER_ID), any())).thenReturn(0);
        when(sessionMapper.markPrimaryMobile(MEMBER_ID, 160L)).thenReturn(1);
        when(sessionMapper.insertDeviceAudit(any())).thenReturn(0);

        assertThrows(ServiceException.class, () -> service.replacePrimary(current, "127.0.0.1"));
        verify(tokenService, never()).updatePrimaryMobile(any(), eq(true));
    }

    private MallMemberDevice device(Long id, String identifier, String type, String primary)
    {
        MallMemberDevice value = new MallMemberDevice();
        value.setMemberDeviceId(id);
        value.setMemberId(MEMBER_ID);
        value.setDeviceIdentifier(identifier);
        value.setDeviceType(type);
        value.setDeviceName(type);
        value.setPrimaryMobile(primary);
        return value;
    }

    private MallMemberSession session(Long id, Long deviceId, String identifier, String type,
            String primary, String tokenHash)
    {
        MallMemberSession value = new MallMemberSession();
        value.setSessionId(id);
        value.setMemberId(MEMBER_ID);
        value.setMemberDeviceId(deviceId);
        value.setDeviceIdentifier(identifier);
        value.setDeviceType(type);
        value.setPrimaryMobile(primary);
        value.setTokenHash(tokenHash);
        value.setExpireTime(LocalDateTime.now().plusDays(1));
        value.setCreateTime(LocalDateTime.now());
        return value;
    }

    private MemberSession currentSession(Long sessionId, Long deviceId, boolean primary)
    {
        MemberSession value = new MemberSession();
        value.setMemberId(MEMBER_ID);
        value.setSessionId(sessionId);
        value.setMemberDeviceId(deviceId);
        value.setPrimaryMobile(primary);
        value.setTokenHash("9".repeat(64));
        return value;
    }
}
