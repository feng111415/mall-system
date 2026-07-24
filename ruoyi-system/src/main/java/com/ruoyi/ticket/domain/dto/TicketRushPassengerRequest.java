package com.ruoyi.ticket.domain.dto;

/**
 * Passenger info used when creating a rush order.
 */
public class TicketRushPassengerRequest
{
    private Long passengerId;

    private String passengerName;

    private String idType;

    private String idNo;

    public Long getPassengerId()
    {
        return passengerId;
    }

    public void setPassengerId(Long passengerId)
    {
        this.passengerId = passengerId;
    }

    public String getPassengerName()
    {
        return passengerName;
    }

    public void setPassengerName(String passengerName)
    {
        this.passengerName = passengerName;
    }

    public String getIdType()
    {
        return idType;
    }

    public void setIdType(String idType)
    {
        this.idType = idType;
    }

    public String getIdNo()
    {
        return idNo;
    }

    public void setIdNo(String idNo)
    {
        this.idNo = idNo;
    }
}
