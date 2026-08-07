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
import java.util.List;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.application.port.SmsPort;
import com.ruoyi.mall.member.domain.MallMember;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.domain.MallMemberConsent;
import com.ruoyi.mall.member.domain.MallSmsCode;
import com.ruoyi.mall.member.domain.dto.MallMemberLoginRequest;
import com.ruoyi.mall.member.domain.vo.MallMemberProfileVo;
import com.ruoyi.mall.member.domain.vo.MallMemberSessionOverviewVo;
import com.ruoyi.mall.member.mapper.MallMemberAuthMapper;
import com.ruoyi.mall.member.mapper.MallMemberMapper;

@Service
public class MallMemberAuthService
{
    private static final String LOGIN_PURPOSE = "REGISTER_LOGIN";
    private static final String PRIMARY_DEVICE_CHANGE_PURPOSE = "PRIMARY_DEVICE_CHANGE";
    public static final String PHONE_CHANGE_OLD_PURPOSE = "PHONE_CHANGE_OLD";
    public static final String PHONE_CHANGE_NEW_PURPOSE = "PHONE_CHANGE_NEW";
    public static final String ACCOUNT_CANCELLATION_PURPOSE = "ACCOUNT_CANCELLATION";
    private static final long CODE_VALID_MILLIS = 5 * 60 * 1000L;
    private static final long SEND_INTERVAL_MILLIS = 60 * 1000L;
    private static final int MAX_VERIFY_ATTEMPTS = 5;
    private static final int DAILY_PHONE_LIMIT = 5;
    private static final int DAILY_IP_LIMIT = 20;
    private static final String SEND_LOCK_PREFIX = "mall_member:sms:send:";
    private final SecureRandom secureRandom = new SecureRandom();
    private final MallMemberMapper memberMapper;
    private final MallMemberAuthMapper authMapper;
    private final MallMemberSessionService sessionService;
    private final MallSmsVerificationAttemptService verificationAttemptService;
    private final MallMemberCaptchaService captchaService;
    private final SmsPort smsPort;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final String mockCode;

    public MallMemberAuthService(MallMemberMapper memberMapper, MallMemberAuthMapper authMapper,
            MallMemberSessionService sessionService, SmsPort smsPort,
            RedisTemplate<Object, Object> redisTemplate,
            @Value("${mall.sms.mock-code:}") String mockCode,
            MallSmsVerificationAttemptService verificationAttemptService,
            MallMemberCaptchaService captchaService)
    {
        this.memberMapper = memberMapper;
        this.authMapper = authMapper;
        this.sessionService = sessionService;
        this.verificationAttemptService = verificationAttemptService;
        this.smsPort = smsPort;
        this.redisTemplate = redisTemplate;
        this.mockCode = mockCode;
        this.captchaService = captchaService;
    }

    public Map<String, Object> sendCode(String phone, String requestIp)
    {
        return sendCode(phone, requestIp, null, null);
    }

    public Map<String, Object> sendCode(String phone, String requestIp, String deviceIdentifier,
            String challengeTicket)
    {
        if (challengeTicket == null || challengeTicket.isBlank())
        {
            if (captchaService.requiresChallenge(phone, requestIp, deviceIdentifier))
            {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("challengeRequired", true);
                response.put("resendAfter", SEND_INTERVAL_MILLIS / 1000);
                return response;
            }
        }
        else
        {
            captchaService.consumeTicket(challengeTicket, phone, requestIp, deviceIdentifier);
        }
        return sendVerificationCode(phone, requestIp, LOGIN_PURPOSE, "MALL_LOGIN_CODE");
    }

    public Map<String, Object> sendPrimaryDeviceCode(MallMemberTokenService.MemberSession current,
            String requestIp)
    {
        sessionService.assertPrimaryChangeAllowed(current);
        MallMember member = requireActiveMember(current.getMemberId());
        return sendVerificationCode(member.getPhone(), requestIp, PRIMARY_DEVICE_CHANGE_PURPOSE,
                "MALL_PRIMARY_DEVICE_CHANGE_CODE");
    }

    public Map<String, Object> sendAccountLifecycleCode(String phone, String purpose, String templateCode,
            String requestIp)
    {
        if (!PHONE_CHANGE_OLD_PURPOSE.equals(purpose) && !PHONE_CHANGE_NEW_PURPOSE.equals(purpose)
                && !ACCOUNT_CANCELLATION_PURPOSE.equals(purpose))
            throw new ServiceException("账号操作验证码用途无效");
        return sendVerificationCode(phone, requestIp, purpose, templateCode);
    }

    @Transactional
    public void verifySmsCode(String phone, String purpose, String code)
    {
        MallSmsCode smsCode = authMapper.selectLatestSmsCode(phone, purpose);
        validateSmsCode(smsCode, phone, code);
        if (authMapper.consumeSmsCode(smsCode.getSmsId()) != 1)
            throw new ServiceException("验证码已使用，请重新获取");
    }

    public Map<String, Object> createCaptchaChallenge(String phone, String requestIp, String deviceIdentifier)
    {
        return captchaService.createChallenge(phone, requestIp, deviceIdentifier);
    }

    public Map<String, Object> verifyCaptchaChallenge(String challengeKey, int position,
            String requestIp, String deviceIdentifier)
    {
        return captchaService.verifyChallenge(challengeKey, position, requestIp, deviceIdentifier);
    }

    private Map<String, Object> sendVerificationCode(String phone, String requestIp, String purpose,
            String templateCode)
    {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                SEND_LOCK_PREFIX + purpose + ":" + phone, requestIp, SEND_INTERVAL_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!Boolean.TRUE.equals(locked))
        {
            throw new ServiceException("验证码发送过于频繁，请稍后再试");
        }

        Date startOfDay = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
        try
        {
            if (authMapper.countSmsByPhoneSince(phone, startOfDay) >= DAILY_PHONE_LIMIT)
            {
                throw new ServiceException("今日验证码发送次数已达上限，请明日再试");
            }
            if (authMapper.countSmsByIpSince(requestIp, startOfDay) >= DAILY_IP_LIMIT)
            {
                throw new ServiceException("当前网络请求过于频繁，请稍后再试");
            }

            MallSmsCode latest = authMapper.selectLatestSmsCode(phone, purpose);
            if (latest != null && latest.getCreateTime() != null
                    && System.currentTimeMillis() - latest.getCreateTime().getTime() < SEND_INTERVAL_MILLIS)
            {
                throw new ServiceException("验证码发送过于频繁，请稍后再试");
            }

        String code = mockCode.matches("^\\d{6}$")
                ? mockCode : String.format("%06d", secureRandom.nextInt(1_000_000));
        String salt = randomHex(16);
            MallSmsCode smsCode = new MallSmsCode();
            smsCode.setPhone(phone);
            smsCode.setPurpose(purpose);
            smsCode.setCodeSalt(salt);
            smsCode.setCodeHash(hashCode(phone, code, salt));
            smsCode.setSendStatus("PENDING");
            smsCode.setVerifyAttempts(0);
            smsCode.setUsedFlag("0");
            smsCode.setExpireTime(new Date(System.currentTimeMillis() + CODE_VALID_MILLIS));
            smsCode.setRequestIp(requestIp);
            authMapper.insertSmsCode(smsCode);

            SmsPort.SmsSendResult result = smsPort.sendVerificationCode(phone, templateCode, code);
            smsCode.setProviderRequestId(result.providerRequestId());
            smsCode.setSendStatus(result.success() ? "SUCCESS" : "FAILED");
            authMapper.updateSmsSendResult(smsCode);
            if (!result.success())
            {
                throw new ServiceException("短信发送失败，请稍后重试");
            }
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("expiresIn", CODE_VALID_MILLIS / 1000);
            response.put("resendAfter", SEND_INTERVAL_MILLIS / 1000);
            return response;
        }
        catch (ServiceException exception)
        {
            redisTemplate.delete(SEND_LOCK_PREFIX + purpose + ":" + phone);
            throw exception;
        }
        catch (RuntimeException exception)
        {
            redisTemplate.delete(SEND_LOCK_PREFIX + purpose + ":" + phone);
            throw exception;
        }
    }

    @Transactional(noRollbackFor = DuplicateKeyException.class)
    public Map<String, Object> login(MallMemberLoginRequest request, String requestIp, String userAgent)
    {
        MallSmsCode smsCode = authMapper.selectLatestSmsCode(request.getPhone(), LOGIN_PURPOSE);
        validateSmsCode(smsCode, request.getPhone(), request.getCode());
        if (authMapper.consumeSmsCode(smsCode.getSmsId()) != 1)
        {
            throw new ServiceException("验证码已使用，请重新获取");
        }

        MallMember member = memberMapper.selectByPhone(request.getPhone());
        boolean created = member == null;
        if (created)
        {
            member = createMember(request.getPhone(), requestIp);
        }
        if (!"0".equals(member.getStatus()))
        {
            throw new ServiceException("账号已停用，请联系客服");
        }

        member.setLastLoginIp(requestIp);
        member.setLastLoginTime(new Date());
        memberMapper.updateLoginInfo(member);
        recordConsent(member.getMemberId(), "USER_AGREEMENT", request.getUserAgreementVersion(), requestIp);
        recordConsent(member.getMemberId(), "PRIVACY_POLICY", request.getPrivacyPolicyVersion(), requestIp);

        MallMemberSessionService.LoginSession loginSession = sessionService.login(member.getMemberId(),
                request.getDeviceId(), userAgent, requestIp);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", loginSession.token());
        response.put("expiresIn", loginSession.expiresIn());
        response.put("newMember", created);
        response.put("member", MallMemberProfileVo.from(member));
        return response;
    }

    @Transactional
    public MallMemberSessionOverviewVo replacePrimaryDevice(
            MallMemberTokenService.MemberSession current, String code, String requestIp)
    {
        MallMember member = requireActiveMember(current.getMemberId());
        MallSmsCode smsCode = authMapper.selectLatestSmsCode(member.getPhone(), PRIMARY_DEVICE_CHANGE_PURPOSE);
        validateSmsCode(smsCode, member.getPhone(), code);
        if (authMapper.consumeSmsCode(smsCode.getSmsId()) != 1)
        {
            throw new ServiceException("验证码已使用，请重新获取");
        }
        return sessionService.replacePrimary(current, requestIp);
    }

    public MallMemberProfileVo profile(Long memberId)
    {
        MallMember member = requireActiveMember(memberId);
        return MallMemberProfileVo.from(member);
    }

    public List<MallMemberAddress> addresses(Long memberId)
    {
        requireActiveMember(memberId);
        return authMapper.selectAddressList(memberId);
    }

    @Transactional
    public MallMemberAddress addAddress(Long memberId, MallMemberAddress address)
    {
        requireActiveMember(memberId);
        address.setMemberId(memberId);
        normalizeDefault(address);
        if ("1".equals(address.getIsDefault())) authMapper.clearDefaultAddress(memberId);
        authMapper.insertAddress(address);
        return authMapper.selectAddress(address.getAddressId(), memberId);
    }

    @Transactional
    public MallMemberAddress updateAddress(Long memberId, Long addressId, MallMemberAddress address)
    {
        requireAddress(memberId, addressId);
        address.setAddressId(addressId);
        address.setMemberId(memberId);
        normalizeDefault(address);
        if ("1".equals(address.getIsDefault())) authMapper.clearDefaultAddress(memberId);
        authMapper.updateAddress(address);
        return authMapper.selectAddress(addressId, memberId);
    }

    public void deleteAddress(Long memberId, Long addressId)
    {
        if (authMapper.deleteAddress(addressId, memberId) != 1)
        {
            throw new ServiceException("收货地址不存在");
        }
    }

    private MallMember createMember(String phone, String requestIp)
    {
        MallMember member = new MallMember();
        member.setPhone(phone);
        member.setNickname("用户" + phone.substring(7));
        member.setStatus("0");
        member.setRegisterSource("H5");
        member.setRegisterIp(requestIp);
        member.setLastLoginIp(requestIp);
        member.setLastLoginTime(new Date());
        member.setCreateBy("member-register");
        try
        {
            memberMapper.insert(member);
            return member;
        }
        catch (DuplicateKeyException e)
        {
            MallMember existing = memberMapper.selectByPhone(phone);
            if (existing == null) throw e;
            return existing;
        }
    }

    private void validateSmsCode(MallSmsCode smsCode, String phone, String rawCode)
    {
        if (smsCode == null || !"SUCCESS".equals(smsCode.getSendStatus()) || "1".equals(smsCode.getUsedFlag()))
        {
            throw new ServiceException("请先获取验证码");
        }
        if (smsCode.getExpireTime() == null || smsCode.getExpireTime().before(new Date()))
        {
            throw new ServiceException("验证码已过期，请重新获取");
        }
        if (smsCode.getVerifyAttempts() != null && smsCode.getVerifyAttempts() >= MAX_VERIFY_ATTEMPTS)
        {
            throw new ServiceException("验证码错误次数过多，请重新获取");
        }
        String expected = hashCode(phone, rawCode, smsCode.getCodeSalt());
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                smsCode.getCodeHash().getBytes(StandardCharsets.US_ASCII)))
        {
            verificationAttemptService.recordFailedAttempt(smsCode.getSmsId());
            throw new ServiceException("验证码不正确");
        }
    }

    private MallMember requireActiveMember(Long memberId)
    {
        MallMember member = memberMapper.selectById(memberId);
        if (member == null || !"0".equals(member.getStatus()))
        {
            throw new ServiceException("会员不存在或已停用");
        }
        return member;
    }

    private void requireAddress(Long memberId, Long addressId)
    {
        if (authMapper.selectAddress(addressId, memberId) == null)
        {
            throw new ServiceException("收货地址不存在");
        }
    }

    private void recordConsent(Long memberId, String type, String version, String requestIp)
    {
        MallMemberConsent consent = new MallMemberConsent();
        consent.setMemberId(memberId);
        consent.setConsentType(type);
        consent.setConsentVersion(version);
        consent.setConsentIp(requestIp);
        consent.setConsentTime(new Date());
        authMapper.insertConsent(consent);
    }

    private void normalizeDefault(MallMemberAddress address)
    {
        address.setIsDefault("1".equals(address.getIsDefault()) ? "1" : "0");
    }

    private String randomHex(int bytes)
    {
        byte[] value = new byte[bytes];
        secureRandom.nextBytes(value);
        return HexFormat.of().formatHex(value);
    }

    private String hashCode(String phone, String code, String salt)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest((salt + ":" + phone + ":" + code)
                    .getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
