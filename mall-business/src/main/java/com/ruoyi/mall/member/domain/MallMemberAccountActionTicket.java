package com.ruoyi.mall.member.domain;

import java.util.Date;

public class MallMemberAccountActionTicket
{
    private Long ticketId;
    private Long requestId;
    private Long memberId;
    private String actionType;
    private String ticketHash;
    private Date expireTime;
    private Date consumedTime;

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long value) { ticketId = value; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long value) { requestId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public String getActionType() { return actionType; }
    public void setActionType(String value) { actionType = value; }
    public String getTicketHash() { return ticketHash; }
    public void setTicketHash(String value) { ticketHash = value; }
    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date value) { expireTime = value; }
    public Date getConsumedTime() { return consumedTime; }
    public void setConsumedTime(Date value) { consumedTime = value; }
}
