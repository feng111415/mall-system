package com.ruoyi.mall.member.domain;

import java.time.LocalDateTime;

public class MallMemberDevice
{
    private Long memberDeviceId;
    private Long memberId;
    private String deviceIdentifier;
    private String deviceType;
    private String deviceName;
    private String primaryMobile;
    private LocalDateTime firstSeenTime;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getMemberDeviceId() { return memberDeviceId; }
    public void setMemberDeviceId(Long value) { this.memberDeviceId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { this.memberId = value; }
    public String getDeviceIdentifier() { return deviceIdentifier; }
    public void setDeviceIdentifier(String value) { this.deviceIdentifier = value; }
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String value) { this.deviceType = value; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String value) { this.deviceName = value; }
    public String getPrimaryMobile() { return primaryMobile; }
    public void setPrimaryMobile(String value) { this.primaryMobile = value; }
    public LocalDateTime getFirstSeenTime() { return firstSeenTime; }
    public void setFirstSeenTime(LocalDateTime value) { this.firstSeenTime = value; }
    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime value) { this.lastLoginTime = value; }
    public String getLastLoginIp() { return lastLoginIp; }
    public void setLastLoginIp(String value) { this.lastLoginIp = value; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { this.createTime = value; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime value) { this.updateTime = value; }
}
