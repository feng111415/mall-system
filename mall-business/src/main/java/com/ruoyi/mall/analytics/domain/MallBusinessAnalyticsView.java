package com.ruoyi.mall.analytics.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MallBusinessAnalyticsView implements Serializable
{
    private LocalDate from;
    private LocalDate to;
    private List<String> sections = new ArrayList<>();
    private List<MallAnalyticsMetric> metrics = new ArrayList<>();
    private List<MallAnalyticsTrendPoint> salesTrend = new ArrayList<>();
    private List<MallAnalyticsRanking> productRanking = new ArrayList<>();

    public LocalDate getFrom() { return from; }
    public void setFrom(LocalDate value) { from = value; }
    public LocalDate getTo() { return to; }
    public void setTo(LocalDate value) { to = value; }
    public List<String> getSections() { return sections; }
    public void setSections(List<String> value) { sections = value == null ? new ArrayList<>() : value; }
    public List<MallAnalyticsMetric> getMetrics() { return metrics; }
    public void setMetrics(List<MallAnalyticsMetric> value) { metrics = value == null ? new ArrayList<>() : value; }
    public List<MallAnalyticsTrendPoint> getSalesTrend() { return salesTrend; }
    public void setSalesTrend(List<MallAnalyticsTrendPoint> value) { salesTrend = value == null ? new ArrayList<>() : value; }
    public List<MallAnalyticsRanking> getProductRanking() { return productRanking; }
    public void setProductRanking(List<MallAnalyticsRanking> value) { productRanking = value == null ? new ArrayList<>() : value; }
}
