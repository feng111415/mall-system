package com.ruoyi.mall.aftersalefunds.domain;

import java.io.Serializable;

public class MallAfterSaleFundsSummary implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int pendingReview;
    private int refundPending;
    private int refundFailed;
    private int openDiffs;
    private int openAlerts;
    private int manualCompensations;

    public int getPendingReview() { return pendingReview; }
    public void setPendingReview(int value) { pendingReview = value; }
    public int getRefundPending() { return refundPending; }
    public void setRefundPending(int value) { refundPending = value; }
    public int getRefundFailed() { return refundFailed; }
    public void setRefundFailed(int value) { refundFailed = value; }
    public int getOpenDiffs() { return openDiffs; }
    public void setOpenDiffs(int value) { openDiffs = value; }
    public int getOpenAlerts() { return openAlerts; }
    public void setOpenAlerts(int value) { openAlerts = value; }
    public int getManualCompensations() { return manualCompensations; }
    public void setManualCompensations(int value) { manualCompensations = value; }
}
