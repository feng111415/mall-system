package com.ruoyi.mall.analytics.domain;

import java.io.Serializable;
import java.math.BigDecimal;

public class MallAnalyticsTrendPoint implements Serializable
{
    private String date;
    private BigDecimal amount;
    private Long orderCount;

    public String getDate() { return date; }
    public void setDate(String value) { date = value; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal value) { amount = value; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long value) { orderCount = value; }
}
