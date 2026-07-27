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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.ip.IpUtils;
import com.ruoyi.mall.member.domain.MallMemberAddress;
import com.ruoyi.mall.member.domain.dto.MallMemberLoginRequest;
import com.ruoyi.mall.member.domain.dto.MallSendCodeRequest;
import com.ruoyi.mall.member.service.MallMemberAuthService;
import com.ruoyi.mall.member.service.MallMemberTokenService;

/** 商城用户端会员接口，使用独立商城 Token。 */
@Anonymous
@RestController
@RequestMapping("/api/mall/member")
public class MallMemberPortalController
{
    private final MallMemberAuthService authService;
    private final MallMemberTokenService tokenService;

    public MallMemberPortalController(MallMemberAuthService authService, MallMemberTokenService tokenService)
    {
        this.authService = authService;
        this.tokenService = tokenService;
    }

    @PostMapping("/sms-code")
    public AjaxResult sendCode(@Valid @RequestBody MallSendCodeRequest request, HttpServletRequest servletRequest)
    {
        return AjaxResult.success("验证码已发送", authService.sendCode(request.getPhone(), IpUtils.getIpAddr(servletRequest)));
    }

    @PostMapping("/login")
    public AjaxResult login(@Valid @RequestBody MallMemberLoginRequest request, HttpServletRequest servletRequest)
    {
        return AjaxResult.success("登录成功", authService.login(request, IpUtils.getIpAddr(servletRequest)));
    }

    @PostMapping("/logout")
    public AjaxResult logout(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        tokenService.logout(authorization);
        return AjaxResult.success("已退出登录");
    }

    @GetMapping("/profile")
    public AjaxResult profile(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        return AjaxResult.success(authService.profile(tokenService.requireMemberId(authorization)));
    }

    @GetMapping("/addresses")
    public AjaxResult addresses(@RequestHeader(value = "Authorization", required = false) String authorization)
    {
        return AjaxResult.success(authService.addresses(tokenService.requireMemberId(authorization)));
    }

    @PostMapping("/addresses")
    public AjaxResult addAddress(@RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody MallMemberAddress address)
    {
        return AjaxResult.success("地址已新增",
                authService.addAddress(tokenService.requireMemberId(authorization), address));
    }

    @PutMapping("/addresses/{addressId}")
    public AjaxResult updateAddress(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long addressId, @Valid @RequestBody MallMemberAddress address)
    {
        return AjaxResult.success("地址已更新",
                authService.updateAddress(tokenService.requireMemberId(authorization), addressId, address));
    }

    @DeleteMapping("/addresses/{addressId}")
    public AjaxResult deleteAddress(@RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable Long addressId)
    {
        authService.deleteAddress(tokenService.requireMemberId(authorization), addressId);
        return AjaxResult.success("地址已删除", Map.of("addressId", addressId));
    }
}
