package com.ruoyi.mall.analytics.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallAnalyticsRanking implements Serializable
{
    private String label;
    private Long quantity;
    private BigDecimal amount;

    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public Long getQuantity() { return quantity; }
    public void setQuantity(Long value) { quantity = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
}
