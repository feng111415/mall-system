package com.ruoyi.mall.aftersale.item.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MallReturnTrackingRequest
{
    @NotBlank(message = "退货物流公司不能为空")
    @Size(max = 32, message = "退货物流公司编码长度不能超过32个字符")
    private String companyCode;
    @NotBlank(message = "退货运单号不能为空")
    @Size(max = 128, message = "退货运单号长度不能超过128个字符")
    private String trackingNo;

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String value) { this.companyCode = value; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String value) { this.trackingNo = value; }
}
