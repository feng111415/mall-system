package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MallPhoneChangeTicketRequest
{
    @NotNull
    private Long requestId;
    @NotBlank
    private String ticket;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long value) { requestId = value; }
    public String getTicket() { return ticket; }
    public void setTicket(String value) { ticket = value; }
}
