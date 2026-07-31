package com.ruoyi.mall.member.service;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.HttpStatus;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/** 商城用户独立会话，不复用若依后台 LoginUser Token。 */
@Service
public class MallMemberTokenService
{
    public static final String MALL_AUTHORIZATION_HEADER = "X-Mall-Authorization";
    private static final String TOKEN_PREFIX = "mall_member:login:";
    private static final int EXPIRE_DAYS = 7;
    private final RedisCache redisCache;

    public MallMemberTokenService(RedisCache redisCache)
    {
        this.redisCache = redisCache;
    }

    public TokenValue issueToken()
    {
        String token = UUID.randomUUID().toString().replace("-", "");
        return new TokenValue(token, hash(token));
    }

    public void activateToken(TokenValue token, MemberSession session)
    {
        redisCache.setCacheObject(TOKEN_PREFIX + token.hash(), session, EXPIRE_DAYS, TimeUnit.DAYS);
    }

    public Long requireMemberId(String authorization)
    {
        return requireSession(authorization).getMemberId();
    }

    public MemberSession requireSession(String authorization)
    {
        String token = extractToken(authorization);
        String tokenHash = hash(token);
        MemberSession session = redisCache.getCacheObject(TOKEN_PREFIX + tokenHash);
        if (session == null || session.getMemberId() == null)
        {
            throw new ServiceException("登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        return session;
    }

    public void logout(String authorization)
    {
        String token = extractToken(authorization);
        deleteByHash(hash(token));
    }

    public void deleteByHash(String tokenHash)
    {
        if (tokenHash != null && tokenHash.matches("^[a-f0-9]{64}$"))
        {
            redisCache.deleteObject(TOKEN_PREFIX + tokenHash);
        }
    }

    public void updatePrimaryMobile(String tokenHash, boolean primaryMobile)
    {
        MemberSession session = redisCache.getCacheObject(TOKEN_PREFIX + tokenHash);
        if (session == null) return;
        session.setPrimaryMobile(primaryMobile);
        redisCache.setCacheObject(TOKEN_PREFIX + tokenHash, session, EXPIRE_DAYS, TimeUnit.DAYS);
    }

    private String hash(String token)
    {
        try
        {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.US_ASCII)));
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    public record TokenValue(String raw, String hash)
    {
    }

    private String extractToken(String authorization)
    {
        if (StringUtils.isBlank(authorization) || !authorization.startsWith("Bearer "))
        {
            throw new ServiceException("请先登录", HttpStatus.UNAUTHORIZED);
        }
        String token = authorization.substring(7).trim();
        if (!token.matches("^[a-f0-9]{32}$"))
        {
            throw new ServiceException("登录凭证格式不正确", HttpStatus.UNAUTHORIZED);
        }
        return token;
    }

    public static class MemberSession implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private Long memberId;
        private Long sessionId;
        private Long memberDeviceId;
        private String deviceId;
        private String deviceType;
        private boolean primaryMobile;
        private String tokenHash;

        public MemberSession() { }
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
        public Long getSessionId() { return sessionId; }
        public void setSessionId(Long value) { this.sessionId = value; }
        public Long getMemberDeviceId() { return memberDeviceId; }
        public void setMemberDeviceId(Long value) { this.memberDeviceId = value; }
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String value) { this.deviceId = value; }
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String value) { this.deviceType = value; }
        public boolean isPrimaryMobile() { return primaryMobile; }
        public void setPrimaryMobile(boolean value) { this.primaryMobile = value; }
        public String getTokenHash() { return tokenHash; }
        public void setTokenHash(String value) { this.tokenHash = value; }
    }
}
