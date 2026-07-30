package com.ruoyi.mall.logistics.domain;

import java.io.Serializable;

public class MallFulfillmentOrder implements Serializable
{
    private Long orderId; private String orderNo; private String status; private String receiverName;
    private Long shipmentId; private String companyName; private String trackingNo;
    public Long getOrderId() { return orderId; } public void setOrderId(Long value) { orderId = value; }
    public String getOrderNo() { return orderNo; } public void setOrderNo(String value) { orderNo = value; }
    public String getStatus() { return status; } public void setStatus(String value) { status = value; }
    public String getReceiverName() { return receiverName; } public void setReceiverName(String value) { receiverName = value; }
    public Long getShipmentId() { return shipmentId; } public void setShipmentId(Long value) { shipmentId = value; }
    public String getCompanyName() { return companyName; } public void setCompanyName(String value) { companyName = value; }
    public String getTrackingNo() { return trackingNo; } public void setTrackingNo(String value) { trackingNo = value; }
}
