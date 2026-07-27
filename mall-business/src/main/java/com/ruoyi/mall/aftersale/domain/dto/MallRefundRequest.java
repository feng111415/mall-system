package com.ruoyi.mall.aftersale.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MallRefundRequest
{
    @NotBlank(message = "退款原因不能为空")
    @Size(max = 255, message = "退款原因长度不能超过255个字符")
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
