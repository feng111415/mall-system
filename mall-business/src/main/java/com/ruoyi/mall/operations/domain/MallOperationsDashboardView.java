package com.ruoyi.mall.operations.domain;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class MallOperationsDashboardView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private LocalDate asOfDate;
    private List<MallOperationsDashboardMetric> metrics;

    public LocalDate getAsOfDate() { return asOfDate; }
    public void setAsOfDate(LocalDate value) { asOfDate = value; }
    public List<MallOperationsDashboardMetric> getMetrics() { return metrics; }
    public void setMetrics(List<MallOperationsDashboardMetric> value) { metrics = value; }
}
