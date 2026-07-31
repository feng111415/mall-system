package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MallMemberLoginRequest
{
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入正确的中国大陆手机号")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须是6位数字")
    private String code;

    @AssertTrue(message = "请先同意用户协议和隐私政策")
    private boolean agreed;

    @NotBlank(message = "用户协议版本不能为空")
    private String userAgreementVersion;

    @NotBlank(message = "隐私政策版本不能为空")
    private String privacyPolicyVersion;

    @NotBlank(message = "设备标识不能为空")
    @Pattern(regexp = "^[a-f0-9]{32}$", message = "设备标识格式不正确")
    private String deviceId;

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public boolean isAgreed() { return agreed; }
    public void setAgreed(boolean agreed) { this.agreed = agreed; }
    public String getUserAgreementVersion() { return userAgreementVersion; }
    public void setUserAgreementVersion(String userAgreementVersion) { this.userAgreementVersion = userAgreementVersion; }
    public String getPrivacyPolicyVersion() { return privacyPolicyVersion; }
    public void setPrivacyPolicyVersion(String privacyPolicyVersion) { this.privacyPolicyVersion = privacyPolicyVersion; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
