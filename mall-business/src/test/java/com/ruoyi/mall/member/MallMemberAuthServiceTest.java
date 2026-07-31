package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.SmsPort;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallSmsCode;
import com.ruoyi.mall.member.domain.dto.MallMemberLoginRequest;
import com.ruoyi.mall.member.domain.vo.MallMemberSessionOverviewVo;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.member.service.MallMemberSessionService;
import com.ruoyi.mall.member.service.MallMemberTokenService.MemberSession;
import com.ruoyi.mall.member.service.MallSmsVerificationAttemptService;

class MallMemberAuthServiceTest
{
    @Mock private MallMemberMapper memberMapper;
    @Mock private MallMemberAuthMapper authMapper;
    @Mock private MallMemberSessionService sessionService;
    @Mock private SmsPort smsPort;
    @Mock private RedisTemplate<Object, Object> redisTemplate;
    @Mock private ValueOperations<Object, Object> valueOperations;
    private MallMemberAuthService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new MallMemberAuthService(memberMapper, authMapper, sessionService, smsPort,
                redisTemplate, "123456", new MallSmsVerificationAttemptService(authMapper));
    }

    @Test
    void rejectsRepeatedSendWhenDistributedLockAlreadyExists()
    {
        when(valueOperations.setIfAbsent(anyString(), any(), eq(60000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(false);
        assertThrows(ServiceException.class, () -> service.sendCode("13800138000", "127.0.0.1"));
        verify(authMapper, never()).insertSmsCode(any());
        verify(smsPort, never()).sendVerificationCode(anyString(), anyString(), anyString());
    }

    @Test
    void loginCreatesDeviceSessionWithBrowserContext()
    {
        String phone = "13800138000";
        String deviceId = "a".repeat(32);
        MallSmsCode smsCode = validSmsCode(phone, "REGISTER_LOGIN", "123456");
        MallMember member = activeMember(phone);
        when(authMapper.selectLatestSmsCode(phone, "REGISTER_LOGIN")).thenReturn(smsCode);
        when(authMapper.consumeSmsCode(smsCode.getSmsId())).thenReturn(1);
        when(memberMapper.selectByPhone(phone)).thenReturn(member);
        when(sessionService.login(9L, deviceId, "Mozilla/5.0 (iPhone) Mobile", "127.0.0.1"))
                .thenReturn(new MallMemberSessionService.LoginSession("raw-token", 604800, 20L));
        MallMemberLoginRequest request = new MallMemberLoginRequest();
        request.setPhone(phone);
        request.setCode("123456");
        request.setAgreed(true);
        request.setUserAgreementVersion("1.0");
        request.setPrivacyPolicyVersion("1.0");
        request.setDeviceId(deviceId);

        Map<String, Object> response = service.login(request, "127.0.0.1", "Mozilla/5.0 (iPhone) Mobile");

        assertEquals("raw-token", response.get("token"));
        verify(sessionService).login(9L, deviceId, "Mozilla/5.0 (iPhone) Mobile", "127.0.0.1");
    }

    @Test
    void primaryDeviceCodeUsesSeparatePurposeAndTemplate()
    {
        MemberSession current = currentSession();
        MallMember member = activeMember("13800138000");
        when(memberMapper.selectById(9L)).thenReturn(member);
        when(valueOperations.setIfAbsent(anyString(), any(), eq(60000L), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(true);
        when(smsPort.sendVerificationCode("13800138000", "MALL_PRIMARY_DEVICE_CHANGE_CODE", "123456"))
                .thenReturn(new SmsPort.SmsSendResult(true, "mock-1", "ok"));

        service.sendPrimaryDeviceCode(current, "127.0.0.1");

        var smsCaptor = org.mockito.ArgumentCaptor.forClass(MallSmsCode.class);
        verify(authMapper).insertSmsCode(smsCaptor.capture());
        assertEquals("PRIMARY_DEVICE_CHANGE", smsCaptor.getValue().getPurpose());
        verify(smsPort).sendVerificationCode("13800138000", "MALL_PRIMARY_DEVICE_CHANGE_CODE", "123456");
    }

    @Test
    void consumesPrimaryCodeBeforeReplacingDevice()
    {
        MemberSession current = currentSession();
        MallMember member = activeMember("13800138000");
        MallSmsCode smsCode = validSmsCode(member.getPhone(), "PRIMARY_DEVICE_CHANGE", "123456");
        MallMemberSessionOverviewVo overview = new MallMemberSessionOverviewVo(List.of(), 2, 30);
        when(memberMapper.selectById(9L)).thenReturn(member);
        when(authMapper.selectLatestSmsCode(member.getPhone(), "PRIMARY_DEVICE_CHANGE")).thenReturn(smsCode);
        when(authMapper.consumeSmsCode(smsCode.getSmsId())).thenReturn(1);
        when(sessionService.replacePrimary(current, "127.0.0.1")).thenReturn(overview);

        assertEquals(overview, service.replacePrimaryDevice(current, "123456", "127.0.0.1"));
        verify(authMapper).consumeSmsCode(smsCode.getSmsId());
        verify(sessionService).replacePrimary(current, "127.0.0.1");
    }

    private MallMember activeMember(String phone)
    {
        MallMember member = new MallMember();
        member.setMemberId(9L);
        member.setPhone(phone);
        member.setNickname("测试会员");
        member.setStatus("0");
        return member;
    }

    private MemberSession currentSession()
    {
        MemberSession current = new MemberSession();
        current.setMemberId(9L);
        current.setSessionId(20L);
        current.setMemberDeviceId(30L);
        return current;
    }

    private MallSmsCode validSmsCode(String phone, String purpose, String code)
    {
        try
        {
            String salt = "test-salt";
            String hash = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((salt + ":" + phone + ":" + code).getBytes(StandardCharsets.UTF_8)));
            MallSmsCode smsCode = new MallSmsCode();
            smsCode.setSmsId(5L);
            smsCode.setPhone(phone);
            smsCode.setPurpose(purpose);
            smsCode.setCodeSalt(salt);
            smsCode.setCodeHash(hash);
            smsCode.setSendStatus("SUCCESS");
            smsCode.setUsedFlag("0");
            smsCode.setVerifyAttempts(0);
            smsCode.setExpireTime(new Date(System.currentTimeMillis() + 60_000));
            return smsCode;
        }
        catch (Exception exception)
        {
            throw new IllegalStateException(exception);
        }
    }
}
