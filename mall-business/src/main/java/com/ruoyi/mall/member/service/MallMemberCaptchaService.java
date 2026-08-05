package com.ruoyi.mall.member.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.domain.MallCaptchaChallenge;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;

/** Risk-triggered challenge and one-time ticket boundary for member SMS login. */
@Service
public class MallMemberCaptchaService
{
    private static final long RISK_WINDOW_MILLIS = 10 * 60 * 1000L;
    private static final long CHALLENGE_VALID_MILLIS = 2 * 60 * 1000L;
    private static final long TICKET_VALID_MILLIS = 2 * 60 * 1000L;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int PHONE_WINDOW_LIMIT = 3;
    private static final int IP_WINDOW_LIMIT = 8;
    private static final int DEVICE_WINDOW_LIMIT = 5;
    private static final int DAILY_PHONE_RISK_LIMIT = 3;
    private static final int DAILY_IP_RISK_LIMIT = 15;
    private static final String WINDOW_PREFIX = "mall_member:sms:risk:";
    private final SecureRandom secureRandom = new SecureRandom();
    private final MallMemberAuthMapper authMapper;
    private final RedisTemplate<Object, Object> redisTemplate;

    public MallMemberCaptchaService(MallMemberAuthMapper authMapper,
            RedisTemplate<Object, Object> redisTemplate)
    {
        this.authMapper = authMapper;
        this.redisTemplate = redisTemplate;
    }

    public boolean requiresChallenge(String phone, String requestIp, String deviceIdentifier)
    {
        long phoneCount = incrementWindow("phone", phone);
        long ipCount = incrementWindow("ip", safe(requestIp));
        long deviceCount = incrementWindow("device", safe(deviceIdentifier));
        Date startOfDay = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        return phoneCount >= PHONE_WINDOW_LIMIT || ipCount >= IP_WINDOW_LIMIT || deviceCount >= DEVICE_WINDOW_LIMIT
                || authMapper.countSmsByPhoneSince(phone, startOfDay) >= DAILY_PHONE_RISK_LIMIT
                || authMapper.countSmsByIpSince(requestIp, startOfDay) >= DAILY_IP_RISK_LIMIT;
    }

    public Map<String, Object> createChallenge(String phone, String requestIp, String deviceIdentifier)
    {
        String challengeKey = randomHex(32);
        int targetPosition = 55 + secureRandom.nextInt(26);
        MallCaptchaChallenge challenge = new MallCaptchaChallenge();
        challenge.setChallengeKey(challengeKey);
        challenge.setPhone(phone);
        challenge.setRequestIp(safe(requestIp));
        challenge.setDeviceIdentifier(safe(deviceIdentifier));
        challenge.setAnswerHash(hash(challengeKey + ":" + targetPosition));
        challenge.setStatus("ISSUED");
        challenge.setVerifyAttempts(0);
        challenge.setExpireTime(new Date(System.currentTimeMillis() + CHALLENGE_VALID_MILLIS));
        authMapper.insertCaptchaChallenge(challenge);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("challengeId", challengeKey);
        response.put("pieceX", 12);
        response.put("targetX", targetPosition);
        response.put("expiresIn", CHALLENGE_VALID_MILLIS / 1000);
        return response;
    }

    public Map<String, Object> verifyChallenge(String challengeKey, int position,
            String requestIp, String deviceIdentifier)
    {
        MallCaptchaChallenge challenge = authMapper.selectCaptchaChallenge(challengeKey);
        if (challenge == null || !"ISSUED".equals(challenge.getStatus())
                || challenge.getExpireTime() == null || challenge.getExpireTime().before(new Date()))
        {
            throw new ServiceException("验证已失效，请重新获取");
        }
        if (!safe(challenge.getRequestIp()).equals(safe(requestIp))
                || !safe(challenge.getDeviceIdentifier()).equals(safe(deviceIdentifier)))
        {
            throw new ServiceException("验证环境已变化，请重新获取");
        }
        if (challenge.getVerifyAttempts() != null && challenge.getVerifyAttempts() >= MAX_VERIFY_ATTEMPTS)
        {
            throw new ServiceException("验证失败次数过多，请重新获取");
        }
        String expected = hash(challengeKey + ":" + position);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                challenge.getAnswerHash().getBytes(StandardCharsets.US_ASCII)))
        {
            int attempt = challenge.getVerifyAttempts() == null ? 1 : challenge.getVerifyAttempts() + 1;
            authMapper.incrementCaptchaAttempts(challenge.getChallengeId());
            if (attempt >= MAX_VERIFY_ATTEMPTS) authMapper.failCaptchaChallenge(challenge.getChallengeId());
            throw new ServiceException("拼图位置不正确，请重试");
        }

        String ticket = randomHex(32);
        Date ticketExpireTime = new Date(System.currentTimeMillis() + TICKET_VALID_MILLIS);
        if (authMapper.verifyCaptchaChallenge(challenge.getChallengeId(), hash(ticket), ticketExpireTime) != 1)
        {
            throw new ServiceException("验证状态已变化，请重新获取");
        }
        return Map.of("ticket", ticket, "expiresIn", TICKET_VALID_MILLIS / 1000);
    }

    public void consumeTicket(String ticket, String phone, String requestIp, String deviceIdentifier)
    {
        if (ticket == null || ticket.isBlank() || authMapper.consumeCaptchaTicket(hash(ticket), phone,
                safe(deviceIdentifier), safe(requestIp)) != 1)
        {
            throw new ServiceException("安全验证已失效，请重新完成验证");
        }
    }

    private long incrementWindow(String type, String value)
    {
        String key = WINDOW_PREFIX + type + ":" + value;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L)
        {
            redisTemplate.expire(key, RISK_WINDOW_MILLIS, TimeUnit.MILLISECONDS);
        }
        return count == null ? 1L : count;
    }

    private String randomHex(int bytes)
    {
        byte[] value = new byte[bytes];
        secureRandom.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }

    private String hash(String value)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private String safe(String value)
    {
        return value == null || value.isBlank() ? "unknown" : value;
    }
}
