package com.ruoyi.mall.inventory.domain.dto;

public class MallStockAdjustRequest
{
    private Integer delta;
    private Integer warningThreshold;
    private String reason;

    public Integer getDelta() { return delta; }
    public void setDelta(Integer delta) { this.delta = delta; }
    public Integer getWarningThreshold() { return warningThreshold; }
    public void setWarningThreshold(Integer warningThreshold) { this.warningThreshold = warningThreshold; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
