package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.SmsPort;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

class MallMemberAuthServiceTest
{
    @Mock private MallMemberMapper memberMapper;
    @Mock private MallMemberAuthMapper authMapper;
    @Mock private MallMemberTokenService tokenService;
    @Mock private SmsPort smsPort;
    @Mock private RedisTemplate<Object, Object> redisTemplate;
    @Mock private ValueOperations<Object, Object> valueOperations;
    private MallMemberAuthService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new MallMemberAuthService(memberMapper, authMapper, tokenService, smsPort, redisTemplate, "123456");
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
}
