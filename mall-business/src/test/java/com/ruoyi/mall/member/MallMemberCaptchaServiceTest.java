package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallCaptchaChallenge;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;
import com.ruoyi.mall.member.service.MallMemberCaptchaService;

class MallMemberCaptchaServiceTest
{
    @Mock private MallMemberAuthMapper authMapper;
    @Mock private RedisTemplate<Object, Object> redisTemplate;
    @Mock private ValueOperations<Object, Object> valueOperations;
    private MallMemberCaptchaService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new MallMemberCaptchaService(authMapper, redisTemplate);
    }

    @Test
    void triggersChallengeWhenPhoneWindowIsBusy()
    {
        when(valueOperations.increment(anyString())).thenReturn(3L, 1L, 1L);
        assertEquals(true, service.requiresChallenge("13800138000", "127.0.0.1", "device-a"));
    }

    @Test
    void verifiesCorrectPositionAndIssuesTicket()
    {
        Map<String, Object> created = service.createChallenge("13800138000", "127.0.0.1", "device-a");
        ArgumentCaptor<MallCaptchaChallenge> captor = ArgumentCaptor.forClass(MallCaptchaChallenge.class);
        verify(authMapper).insertCaptchaChallenge(captor.capture());
        MallCaptchaChallenge challenge = captor.getValue();
        assertFalse(created.containsKey("targetX"));
        assertTrue(((String) created.get("sceneImage")).startsWith("data:image/png;base64,"));
        assertTrue(((String) created.get("pieceImage")).startsWith("data:image/png;base64,"));
        challenge.setChallengeId(7L);
        when(authMapper.selectCaptchaChallenge((String) created.get("challengeId"))).thenReturn(challenge);
        when(authMapper.verifyCaptchaChallenge(eq(7L), anyString(), any())).thenReturn(1);

        Map<String, Object> verified = service.verifyChallenge((String) created.get("challengeId"),
                findAnswer(challenge), "127.0.0.1", "device-a");

        assertEquals(120L, ((Number) verified.get("expiresIn")).longValue());
        verify(authMapper).verifyCaptchaChallenge(eq(7L), anyString(), any());
    }

    @Test
    void rejectsWrongPositionAndRecordsAttempt()
    {
        String key = "challenge-key";
        MallCaptchaChallenge challenge = challenge(key, 7L, hash(key + ":70"));
        when(authMapper.selectCaptchaChallenge(key)).thenReturn(challenge);
        assertThrows(ServiceException.class, () -> service.verifyChallenge(key, 10, "127.0.0.1", "device-a"));
        verify(authMapper).incrementCaptchaAttempts(7L);
    }

    @Test
    void acceptsSmallDragRoundingDifference()
    {
        Map<String, Object> created = service.createChallenge("13800138000", "127.0.0.1", "device-a");
        ArgumentCaptor<MallCaptchaChallenge> captor = ArgumentCaptor.forClass(MallCaptchaChallenge.class);
        verify(authMapper).insertCaptchaChallenge(captor.capture());
        MallCaptchaChallenge challenge = captor.getValue();
        challenge.setChallengeId(8L);
        when(authMapper.selectCaptchaChallenge((String) created.get("challengeId"))).thenReturn(challenge);
        when(authMapper.verifyCaptchaChallenge(eq(8L), anyString(), any())).thenReturn(1);

        service.verifyChallenge((String) created.get("challengeId"),
                findAnswer(challenge) + 10, "127.0.0.1", "device-a");

        verify(authMapper).verifyCaptchaChallenge(eq(8L), anyString(), any());
    }

    @Test
    void consumesTicketOnlyOnceAtServer()
    {
        when(authMapper.consumeCaptchaTicket(anyString(), eq("13800138000"), eq("device-a"), eq("127.0.0.1")))
                .thenReturn(1, 0);
        service.consumeTicket("ticket", "13800138000", "127.0.0.1", "device-a");
        assertThrows(ServiceException.class, () -> service.consumeTicket("ticket", "13800138000", "127.0.0.1", "device-a"));
    }

    private MallCaptchaChallenge challenge(String key, Long id, String answerHash)
    {
        MallCaptchaChallenge challenge = new MallCaptchaChallenge();
        challenge.setChallengeId(id);
        challenge.setChallengeKey(key);
        challenge.setPhone("13800138000");
        challenge.setDeviceIdentifier("device-a");
        challenge.setRequestIp("127.0.0.1");
        challenge.setAnswerHash(answerHash);
        challenge.setStatus("ISSUED");
        challenge.setVerifyAttempts(0);
        challenge.setExpireTime(new java.util.Date(System.currentTimeMillis() + 60_000));
        return challenge;
    }

    private String hash(String value)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (Exception exception)
        {
            throw new IllegalStateException(exception);
        }
    }

    private int findAnswer(MallCaptchaChallenge challenge)
    {
        for (int position = 0; position <= 1000; position++)
        {
            if (hash(challenge.getChallengeKey() + ":" + position).equals(challenge.getAnswerHash()))
                return position;
        }
        throw new IllegalStateException("challenge answer was not in the supported range");
    }
}
