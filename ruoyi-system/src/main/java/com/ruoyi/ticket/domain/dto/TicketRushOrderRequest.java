package com.ruoyi.ticket.domain.dto;

import java.util.List;

/**
 * Rush order request.
 */
public class TicketRushOrderRequest
{
    private Long activityId;

    private Long sessionId;

    private Long typeId;

    private Integer quantity;

    private Long userId;

    private String openid;

    private String requestId;

    private String clientIp;

    private List<TicketRushPassengerRequest> passengers;

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }

    public Long getTypeId()
    {
        return typeId;
    }

    public void setTypeId(Long typeId)
    {
        this.typeId = typeId;
    }

    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }

    public String getRequestId()
    {
        return requestId;
    }

    public void setRequestId(String requestId)
    {
        this.requestId = requestId;
    }

    public String getClientIp()
    {
        return clientIp;
    }

    public void setClientIp(String clientIp)
    {
        this.clientIp = clientIp;
    }

    public List<TicketRushPassengerRequest> getPassengers()
    {
        return passengers;
    }

    public void setPassengers(List<TicketRushPassengerRequest> passengers)
    {
        this.passengers = passengers;
    }
}
