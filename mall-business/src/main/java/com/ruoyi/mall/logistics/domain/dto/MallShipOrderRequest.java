package com.ruoyi.mall.logistics.domain.dto;

import jakarta.validation.constraints.Size;

public class MallShipOrderRequest
{
    @Size(max = 32, message = "物流公司编码长度不能超过32个字符")
    private String companyCode;

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
}
