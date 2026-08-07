package com.ruoyi.mall.member.domain;

import java.util.Date;

public class MallMemberPhoneChangeRequest
{
    private Long requestId;
    private Long memberId;
    private String oldPhone;
    private String newPhone;
    private String status;
    private Date oldVerifiedTime;
    private Date newVerifiedTime;
    private Date expiresTime;
    private Date completedTime;
    private String requestIp;
    private Date createTime;
    private Date updateTime;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long value) { requestId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public String getOldPhone() { return oldPhone; }
    public void setOldPhone(String value) { oldPhone = value; }
    public String getNewPhone() { return newPhone; }
    public void setNewPhone(String value) { newPhone = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public Date getOldVerifiedTime() { return oldVerifiedTime; }
    public void setOldVerifiedTime(Date value) { oldVerifiedTime = value; }
    public Date getNewVerifiedTime() { return newVerifiedTime; }
    public void setNewVerifiedTime(Date value) { newVerifiedTime = value; }
    public Date getExpiresTime() { return expiresTime; }
    public void setExpiresTime(Date value) { expiresTime = value; }
    public Date getCompletedTime() { return completedTime; }
    public void setCompletedTime(Date value) { completedTime = value; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String value) { requestIp = value; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date value) { createTime = value; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date value) { updateTime = value; }
}
