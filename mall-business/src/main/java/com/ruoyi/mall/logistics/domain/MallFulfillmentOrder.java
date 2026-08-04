package com.ruoyi.mall.logistics.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MallFulfillmentOrder implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long orderId; private String orderNo; private String status; private String receiverName;
    private String receiverPhone; private LocalDateTime orderCreateTime;
    private Long shipmentId; private String companyCode; private String companyName; private String trackingNo;
    private String shipmentStatus; private LocalDateTime shippedTime; private LocalDateTime deliveredTime;
    private String latestNodeStatus; private String latestNodeTitle; private String latestNodeDescription;
    private String latestNodeLocation; private LocalDateTime latestNodeTime;
    private String workflowStatus; private String attentionType; private Boolean overdueUnshipped;
    public Long getOrderId() { return orderId; } public void setOrderId(Long value) { orderId = value; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String value) { orderNo = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public String getReceiverName() { return receiverName; } public void setReceiverName(String value) { receiverName = value; }
    public String getReceiverPhone() { return receiverPhone; } public void setReceiverPhone(String value) { receiverPhone = value; }
    public LocalDateTime getOrderCreateTime() { return orderCreateTime; } public void setOrderCreateTime(LocalDateTime value) { orderCreateTime = value; }
    public Long getShipmentId() { return shipmentId; } public void setShipmentId(Long value) { shipmentId = value; }
    public String getCompanyCode() { return companyCode; } public void setCompanyCode(String value) { companyCode = value; }
    public String getCompanyName() { return companyName; } public void setCompanyName(String value) { companyName = value; }
    public String getTrackingNo() { return trackingNo; } public void setTrackingNo(String value) { trackingNo = value; }
    public String getShipmentStatus() { return shipmentStatus; } public void setShipmentStatus(String value) { shipmentStatus = value; }
    public LocalDateTime getShippedTime() { return shippedTime; } public void setShippedTime(LocalDateTime value) { shippedTime = value; }
    public LocalDateTime getDeliveredTime() { return deliveredTime; } public void setDeliveredTime(LocalDateTime value) { deliveredTime = value; }
    public String getLatestNodeStatus() { return latestNodeStatus; } public void setLatestNodeStatus(String value) { latestNodeStatus = value; }
    public String getLatestNodeTitle() { return latestNodeTitle; } public void setLatestNodeTitle(String value) { latestNodeTitle = value; }
    public String getLatestNodeDescription() { return latestNodeDescription; } public void setLatestNodeDescription(String value) { latestNodeDescription = value; }
    public String getLatestNodeLocation() { return latestNodeLocation; } public void setLatestNodeLocation(String value) { latestNodeLocation = value; }
    public LocalDateTime getLatestNodeTime() { return latestNodeTime; } public void setLatestNodeTime(LocalDateTime value) { latestNodeTime = value; }
    public String getWorkflowStatus() { return workflowStatus; } public void setWorkflowStatus(String value) { workflowStatus = value; }
    public String getAttentionType() { return attentionType; } public void setAttentionType(String value) { attentionType = value; }
    public Boolean getOverdueUnshipped() { return overdueUnshipped; } public void setOverdueUnshipped(Boolean value) { overdueUnshipped = value; }
}
