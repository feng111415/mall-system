package com.ruoyi.mall.member.domain;

import java.time.LocalDateTime;

public class MallMemberSession
{
    private Long sessionId;
    private Long memberId;
    private Long memberDeviceId;
    private String tokenHash;
    private String status;
    private String loginIp;
    private LocalDateTime lastActiveTime;
    private LocalDateTime expireTime;
    private String offlineReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String deviceIdentifier;
    private String deviceType;
    private String deviceName;
    private String primaryMobile;

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long value) { this.sessionId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { this.memberId = value; }
    public Long getMemberDeviceId() { return memberDeviceId; }
    public void setMemberDeviceId(Long value) { this.memberDeviceId = value; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String value) { this.tokenHash = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }
    public String getLoginIp() { return loginIp; }
    public void setLoginIp(String value) { this.loginIp = value; }
    public LocalDateTime getLastActiveTime() { return lastActiveTime; }
    public void setLastActiveTime(LocalDateTime value) { this.lastActiveTime = value; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime value) { this.expireTime = value; }
    public String getOfflineReason() { return offlineReason; }
    public void setOfflineReason(String value) { this.offlineReason = value; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { this.createTime = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { this.updateTime = value; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String value) { this.deviceIdentifier = value; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String value) { this.deviceType = value; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String value) { this.deviceName = value; }
    public String getPrimaryMobile() { return primaryMobile; }
    public void setPrimaryMobile(String value) { this.primaryMobile = value; }
}
