package com.ruoyi.mall.analytics.domain;

import java.io.Serializable;
import java.math.BigDecimal;

/** 聚合指标，不包含会员或订单明细。 */
public class MallAnalyticsMetric implements Serializable
{
    private String key;
    private String label;
    private String value;
    private BigDecimal amount;
    private Long count;
    private String unit;
    private String trend;

    public String getKey() { return key; }
    public void setKey(String value) { key = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public Long getCount() { return count; }
    public void setCount(Long value) { count = value; }
    public String getUnit() { return unit; }
    public void setUnit(String value) { unit = value; }
    public String getTrend() { return trend; }
    public void setTrend(String value) { trend = value; }
}
