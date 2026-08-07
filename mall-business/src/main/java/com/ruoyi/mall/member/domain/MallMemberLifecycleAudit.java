package com.ruoyi.mall.member.domain;

public class MallMemberLifecycleAudit
{
    private Long auditId;
    private Long memberId;
    private String eventType;
    private String title;
    private String summary;
    private String oldPhone;
    private String newPhone;
    private String requestIp;

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long value) { auditId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public String getEventType() { return eventType; }
    public void setEventType(String value) { eventType = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary = value; }
    public String getOldPhone() { return oldPhone; }
    public void setOldPhone(String value) { oldPhone = value; }
    public String getNewPhone() { return newPhone; }
    public void setNewPhone(String value) { newPhone = value; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String value) { requestIp = value; }
}
