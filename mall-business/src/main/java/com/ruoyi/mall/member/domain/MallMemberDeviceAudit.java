package com.ruoyi.mall.member.domain;

public class MallMemberDeviceAudit
{
    private Long auditId;
    private Long memberId;
    private String actionType;
    private Long oldMemberDeviceId;
    private Long newMemberDeviceId;
    private String requestIp;

    public Long getAuditId() { return auditId; }
    public void setAuditId(Long value) { this.auditId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { this.memberId = value; }
    public String getActionType() { return actionType; }
    public void setActionType(String value) { this.actionType = value; }
    public Long getOldMemberDeviceId() { return oldMemberDeviceId; }
    public void setOldMemberDeviceId(Long value) { this.oldMemberDeviceId = value; }
    public Long getNewMemberDeviceId() { return newMemberDeviceId; }
    public void setNewMemberDeviceId(Long value) { this.newMemberDeviceId = value; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String value) { this.requestIp = value; }
}
