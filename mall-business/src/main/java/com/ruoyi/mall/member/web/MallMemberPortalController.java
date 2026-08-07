package com.ruoyi.mall.member.web;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.domain.dto.MallMemberLoginRequest;
import com.ruoyi.mall.member.domain.dto.MallNicknameUpdateRequest;
import com.ruoyi.mall.member.domain.dto.MallAvatarPresetRequest;
import com.ruoyi.mall.member.domain.dto.MallPrimaryDeviceReplaceRequest;
import com.ruoyi.mall.member.domain.dto.MallSendCodeRequest;
import com.ruoyi.mall.member.domain.dto.MallCaptchaChallengeRequest;
import com.ruoyi.mall.member.domain.dto.MallCaptchaVerifyRequest;
import com.ruoyi.mall.member.domain.dto.MallPhoneChangeStartRequest;
import com.ruoyi.mall.member.domain.dto.MallPhoneChangeTicketRequest;
import com.ruoyi.mall.member.domain.dto.MallPhoneChangeVerifyRequest;
import com.ruoyi.mall.member.domain.dto.MallAccountCancellationRequest;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.member.service.MallMemberAccountQueryService;
import com.ruoyi.mall.member.service.MallMemberAccountLifecycleService;
import com.ruoyi.mall.member.service.MallMemberProfileService;
import com.ruoyi.mall.member.service.MallMemberSessionService;
import com.ruoyi.mall.member.service.MallMemberTokenService;
import com.ruoyi.mall.member.service.MallMemberTokenService.MemberSession;

/** 商城用户端会员接口，使用独立商城 Token。 */
@Anonymous
@RestController
@RequestMapping("/api/mall/member")
public class MallMemberPortalController
{
    private final MallMemberAuthService authService;
    private final MallMemberAccountQueryService accountQueryService;
    private final MallMemberAccountLifecycleService accountLifecycleService;
    private final MallMemberProfileService profileService;
    private final MallMemberSessionService sessionService;
    private final MallMemberTokenService tokenService;

    public MallMemberPortalController(MallMemberAuthService authService,
            MallMemberAccountQueryService accountQueryService,
            MallMemberAccountLifecycleService accountLifecycleService,
            MallMemberProfileService profileService, MallMemberSessionService sessionService,
            MallMemberTokenService tokenService)
    {
        this.authService = authService;
        this.accountQueryService = accountQueryService;
        this.accountLifecycleService = accountLifecycleService;
        this.profileService = profileService;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    @PostMapping("/sms-code")
    public AjaxResult sendCode(@Valid @RequestBody MallSendCodeRequest request, HttpServletRequest servletRequest)
    {
        String deviceIdentifier = servletRequest.getHeader("X-Mall-Device-Id");
        return AjaxResult.success("验证码已发送", authService.sendCode(request.getPhone(), IpUtils.getIpAddr(servletRequest),
                deviceIdentifier, request.getChallengeTicket()));
    }

    @PostMapping("/sms-challenge")
    public AjaxResult createSmsChallenge(@Valid @RequestBody MallCaptchaChallengeRequest request,
            HttpServletRequest servletRequest)
    {
        return AjaxResult.success("需要安全验证", authService.createCaptchaChallenge(request.getPhone(),
                IpUtils.getIpAddr(servletRequest), servletRequest.getHeader("X-Mall-Device-Id")));
    }

    @PostMapping("/sms-challenge/verify")
    public AjaxResult verifySmsChallenge(@Valid @RequestBody MallCaptchaVerifyRequest request,
            HttpServletRequest servletRequest)
    {
        return AjaxResult.success("验证通过", authService.verifyCaptchaChallenge(request.getChallengeId(),
                request.getPosition(), IpUtils.getIpAddr(servletRequest), servletRequest.getHeader("X-Mall-Device-Id")));
    }

    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody MallMemberLoginRequest request, HttpServletRequest servletRequest)
    {
        return AjaxResult.success("登录成功", authService.login(request, IpUtils.getIpAddr(servletRequest),
                servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/logout")
    public AjaxResult logout(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    {
        sessionService.logout(tokenService.requireSession(authorization));
        return AjaxResult.success("已退出登录");
    }

    @GetMapping("/profile/sessions")
    public AjaxResult sessions(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    {
        return AjaxResult.success(sessionService.overview(tokenService.requireSession(authorization)));
    }

    @DeleteMapping("/profile/sessions/{sessionId}")
    public AjaxResult revokeSession(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long sessionId)
    {
        MemberSession current = tokenService.requireSession(authorization);
        return AjaxResult.success("设备已下线", sessionService.revoke(current, sessionId));
    }

    @PostMapping("/profile/sessions/primary-mobile/code")
    public AjaxResult sendPrimaryDeviceCode(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            HttpServletRequest servletRequest)
    {
        MemberSession current = tokenService.requireSession(authorization);
        return AjaxResult.success("验证码已发送",
                authService.sendPrimaryDeviceCode(current, IpUtils.getIpAddr(servletRequest)));
    }

    @PutMapping("/profile/sessions/primary-mobile")
    public AjaxResult replacePrimaryDevice(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallPrimaryDeviceReplaceRequest request, HttpServletRequest servletRequest)
    {
        MemberSession current = tokenService.requireSession(authorization);
        return AjaxResult.success("主设备已更换", authService.replacePrimaryDevice(current, request.getCode(),
                IpUtils.getIpAddr(servletRequest)));
    }

    @GetMapping("/profile")
    public AjaxResult profile(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    {
        return AjaxResult.success(profileService.profile(tokenService.requireMemberId(authorization)));
    }

    @GetMapping("/profile/account-lifecycle")
    public AjaxResult accountLifecycle(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    {
        return AjaxResult.success(accountQueryService.overview(tokenService.requireMemberId(authorization)));
    }

    @PostMapping("/profile/phone-change")
    public AjaxResult startPhoneChange(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallPhoneChangeStartRequest request, HttpServletRequest servletRequest)
    {
        return AjaxResult.success(accountLifecycleService.startPhoneChange(tokenService.requireSession(authorization),
                request, IpUtils.getIpAddr(servletRequest)));
    }

    @PostMapping("/profile/phone-change/old-code")
    public AjaxResult sendOldPhoneChangeCode(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestParam Long requestId, HttpServletRequest servletRequest)
    {
        return AjaxResult.success(accountLifecycleService.sendOldPhoneCode(tokenService.requireSession(authorization),
                requestId, IpUtils.getIpAddr(servletRequest)));
    }

    @PostMapping("/profile/phone-change/old-verify")
    public AjaxResult verifyOldPhoneChange(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallPhoneChangeVerifyRequest request, HttpServletRequest servletRequest)
    {
        return AjaxResult.success(accountLifecycleService.verifyOldPhone(tokenService.requireSession(authorization),
                request.getRequestId(), request.getCode(), IpUtils.getIpAddr(servletRequest)));
    }

    @PostMapping("/profile/phone-change/new-code")
    public AjaxResult sendNewPhoneChangeCode(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallPhoneChangeTicketRequest request, HttpServletRequest servletRequest)
    {
        return AjaxResult.success(accountLifecycleService.sendNewPhoneCode(tokenService.requireSession(authorization),
                request.getRequestId(), request.getTicket(), IpUtils.getIpAddr(servletRequest)));
    }

    @PutMapping("/profile/phone-change")
    public AjaxResult completePhoneChange(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallPhoneChangeVerifyRequest request, HttpServletRequest servletRequest)
    {
        accountLifecycleService.completePhoneChange(tokenService.requireSession(authorization), request.getRequestId(),
                request.getTicket(), request.getCode(), IpUtils.getIpAddr(servletRequest));
        return AjaxResult.success("手机号更换成功，请使用新手机号重新登录");
    }

    @PostMapping("/profile/account-cancellation/code")
    public AjaxResult sendAccountCancellationCode(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            HttpServletRequest servletRequest)
    {
        return AjaxResult.success(accountLifecycleService.sendCancellationCode(tokenService.requireSession(authorization),
                IpUtils.getIpAddr(servletRequest)));
    }

    @DeleteMapping("/profile/account")
    public AjaxResult cancelAccount(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallAccountCancellationRequest request, HttpServletRequest servletRequest)
    {
        accountLifecycleService.cancelAccount(tokenService.requireSession(authorization), request.getCode(),
                IpUtils.getIpAddr(servletRequest));
        return AjaxResult.success("账号已停用");
    }

    @GetMapping("/profile/avatar-presets")
    public AjaxResult avatarPresets()
    {
        return AjaxResult.success(profileService.avatarPresets());
    }

    @PutMapping("/profile/nickname")
    public AjaxResult updateNickname(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallNicknameUpdateRequest request, HttpServletRequest servletRequest)
    {
        Long memberId = tokenService.requireMemberId(authorization);
        profileService.updateNickname(memberId, request.getNickname(), IpUtils.getIpAddr(servletRequest));
        return AjaxResult.success("昵称已更新", profileService.profile(memberId));
    }

    @PutMapping("/profile/avatar/preset")
    public AjaxResult selectPresetAvatar(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallAvatarPresetRequest request, HttpServletRequest servletRequest)
    {
        Long memberId = tokenService.requireMemberId(authorization);
        profileService.selectPresetAvatar(memberId, request.getPresetCode(), IpUtils.getIpAddr(servletRequest));
        return AjaxResult.success("头像已更新", profileService.profile(memberId));
    }

    @PostMapping("/profile/avatar/upload")
    public AjaxResult uploadAvatar(
            @RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @RequestParam("file") MultipartFile file, HttpServletRequest servletRequest)
    {
        Long memberId = tokenService.requireMemberId(authorization);
        profileService.uploadAvatar(memberId, file, IpUtils.getIpAddr(servletRequest));
        return AjaxResult.success("头像已更新", profileService.profile(memberId));
    }

    @GetMapping("/addresses")
    public AjaxResult addresses(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization)
    {
        return AjaxResult.success(authService.addresses(tokenService.requireMemberId(authorization)));
    }

    @PostMapping("/addresses")
    public AjaxResult addAddress(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @Valid @RequestBody MallMemberAddress address)
    {
        return AjaxResult.success("地址已新增",
                authService.addAddress(tokenService.requireMemberId(authorization), address));
    }

    @PutMapping("/addresses/{addressId}")
    public AjaxResult updateAddress(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long addressId, @Valid @RequestBody MallMemberAddress address)
    {
        return AjaxResult.success("地址已更新",
                authService.updateAddress(tokenService.requireMemberId(authorization), addressId, address));
    }

    @DeleteMapping("/addresses/{addressId}")
    public AjaxResult deleteAddress(@RequestHeader(value = MallMemberTokenService.MALL_AUTHORIZATION_HEADER, required = false) String authorization,
            @PathVariable Long addressId)
    {
        authService.deleteAddress(tokenService.requireMemberId(authorization), addressId);
        return AjaxResult.success("地址已删除", Map.of("addressId", addressId));
    }
}
