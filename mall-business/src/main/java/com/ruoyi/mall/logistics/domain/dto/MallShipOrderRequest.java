package com.ruoyi.mall.logistics.domain.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

public class MallShipOrderRequest
{
    @NotBlank(message = "物流公司编码不能为空")
    @Size(max = 32, message = "物流公司编码长度不能超过32个字符")
    private String companyCode;
    /** 兼容旧客户端字段，服务端不信任该值，名称按 companyCode 白名单反查。 */
    @Size(max = 64, message = "物流公司名称长度不能超过64个字符")
    private String companyName;
    @NotBlank(message = "运单号不能为空")
    @Size(max = 128, message = "运单号长度不能超过128个字符")
    private String trackingNo;

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
}
