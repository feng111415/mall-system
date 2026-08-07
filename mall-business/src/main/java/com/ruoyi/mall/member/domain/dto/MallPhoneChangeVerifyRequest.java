package com.ruoyi.mall.member.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class MallPhoneChangeVerifyRequest
{
    @NotNull
    private Long requestId;
    private String ticket;
    @NotBlank
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;

    public Long getRequestId() { return requestId; }
    public void setRequestId(Long value) { requestId = value; }
    public String getTicket() { return ticket; }
    public void setTicket(String value) { ticket = value; }
    public String getCode() { return code; }
    public void setCode(String value) { code = value; }
}
