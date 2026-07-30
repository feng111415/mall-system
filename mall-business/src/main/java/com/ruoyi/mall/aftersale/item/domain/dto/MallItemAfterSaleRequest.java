package com.ruoyi.mall.aftersale.item.domain.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class MallItemAfterSaleRequest
{
    @NotBlank(message = "售后类型不能为空")
    private String type;
    @NotBlank(message = "售后原因不能为空")
    private String reasonCode;
    @Size(max = 255, message = "售后说明长度不能超过255个字符")
    private String reason;
    @Size(max = 1000, message = "凭证地址长度不能超过1000个字符")
    private String evidenceUrl;
    @Valid
    @NotEmpty(message = "至少选择一个订单项")
    private List<MallItemAfterSaleItemRequest> items;

    public String getType() { return type; }
    public void setType(String value) { this.type = value; }
    public String getReasonCode() { return reasonCode; }
    public void setReasonCode(String value) { this.reasonCode = value; }
    public String getReason() { return reason; }
    public void setReason(String value) { this.reason = value; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String value) { this.evidenceUrl = value; }
    public List<MallItemAfterSaleItemRequest> getItems() { return items; }
    public void setItems(List<MallItemAfterSaleItemRequest> value) { this.items = value; }
}
