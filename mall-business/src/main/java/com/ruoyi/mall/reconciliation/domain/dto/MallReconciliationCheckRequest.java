package com.ruoyi.mall.reconciliation.domain.dto;

import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MallReconciliationCheckRequest
{
    @NotBlank(message = "对账类型不能为空")
    @Pattern(regexp = "PAYMENT|REFUND", message = "对账类型只能是PAYMENT或REFUND")
    private String diffType;
    @NotBlank(message = "业务单号不能为空")
    private String businessNo;
    @NotBlank(message = "外部状态不能为空")
    private String externalStatus;
    private BigDecimal externalAmount;
    private String externalProviderNo;
    private String externalMessage;

    public String getDiffType() { return diffType; }
    public void setDiffType(String diffType) { this.diffType = diffType; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String businessNo) { this.businessNo = businessNo; }
    public String getExternalStatus() { return externalStatus; }
    public void setExternalStatus(String externalStatus) { this.externalStatus = externalStatus; }
    public BigDecimal getExternalAmount() { return externalAmount; }
    public void setExternalAmount(BigDecimal externalAmount) { this.externalAmount = externalAmount; }
    public String getExternalProviderNo() { return externalProviderNo; }
    public void setExternalProviderNo(String externalProviderNo) { this.externalProviderNo = externalProviderNo; }
    public String getExternalMessage() { return externalMessage; }
    public void setExternalMessage(String externalMessage) { this.externalMessage = externalMessage; }
}
