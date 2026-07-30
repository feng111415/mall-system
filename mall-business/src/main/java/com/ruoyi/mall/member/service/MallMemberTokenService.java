package com.ruoyi.mall.member.service;

import java.io.Serializable;
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

    public String createToken(Long memberId)
    {
        String token = UUID.randomUUID().toString().replace("-", "");
        redisCache.setCacheObject(TOKEN_PREFIX + token, new MemberSession(memberId), EXPIRE_DAYS, TimeUnit.DAYS);
        return token;
    }

    public Long requireMemberId(String authorization)
    {
        String token = extractToken(authorization);
        MemberSession session = redisCache.getCacheObject(TOKEN_PREFIX + token);
        if (session == null || session.getMemberId() == null)
        {
            throw new ServiceException("登录已失效，请重新登录", HttpStatus.UNAUTHORIZED);
        }
        redisCache.expire(TOKEN_PREFIX + token, EXPIRE_DAYS, TimeUnit.DAYS);
        return session.getMemberId();
    }

    public void logout(String authorization)
    {
        String token = extractToken(authorization);
        redisCache.deleteObject(TOKEN_PREFIX + token);
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

        public MemberSession() { }
        public MemberSession(Long memberId) { this.memberId = memberId; }
        public Long getMemberId() { return memberId; }
        public void setMemberId(Long memberId) { this.memberId = memberId; }
    }
}
