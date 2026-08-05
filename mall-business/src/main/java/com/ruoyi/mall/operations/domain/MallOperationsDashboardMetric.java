package com.ruoyi.mall.operations.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallOperationsDashboardMetric implements Serializable
{
    private static final long serialVersionUID = 1L;
    private String key;
    private String label;
    private String hint;
    private String severity;
    private String path;
    private String valueType;
    private Long count;
    private BigDecimal amount;

    public String getKey() { return key; }
    public void setKey(String value) { key = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getHint() { return hint; }
    public void setHint(String value) { hint = value; }
    public String getSeverity() { return severity; }
    public void setSeverity(String value) { severity = value; }
    public String getPath() { return path; }
    public void setPath(String value) { path = value; }
    public String getValueType() { return valueType; }
    public void setValueType(String value) { valueType = value; }
    public Long getCount() { return count; }
    public void setCount(Long value) { count = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
}
