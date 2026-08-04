package com.ruoyi.mall.logistics.domain;

import java.io.Serializable;

public class MallFulfillmentSummary implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Integer totalCount;
    private Integer waitingShipmentCount;
    private Integer inTransitCount;
    private Integer outForDeliveryCount;
    private Integer exceptionCount;
    private Integer deliveredWaitReceiptCount;
    private Integer completedCount;
    private Integer overdueUnshippedCount;
    private Integer dataInconsistentCount;

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer value) { totalCount = value; }
    public Integer getWaitingShipmentCount() { return waitingShipmentCount; }
    public void setWaitingShipmentCount(Integer value) { waitingShipmentCount = value; }
    public Integer getInTransitCount() { return inTransitCount; }
    public void setInTransitCount(Integer value) { inTransitCount = value; }
    public Integer getOutForDeliveryCount() { return outForDeliveryCount; }
    public void setOutForDeliveryCount(Integer value) { outForDeliveryCount = value; }
    public Integer getExceptionCount() { return exceptionCount; }
    public void setExceptionCount(Integer value) { exceptionCount = value; }
    public Integer getDeliveredWaitReceiptCount() { return deliveredWaitReceiptCount; }
    public void setDeliveredWaitReceiptCount(Integer value) { deliveredWaitReceiptCount = value; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer value) { completedCount = value; }
    public Integer getOverdueUnshippedCount() { return overdueUnshippedCount; }
    public void setOverdueUnshippedCount(Integer value) { overdueUnshippedCount = value; }
    public Integer getDataInconsistentCount() { return dataInconsistentCount; }
    public void setDataInconsistentCount(Integer value) { dataInconsistentCount = value; }
}
