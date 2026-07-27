package com.ruoyi.mall.logistics.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MallLogisticsNode implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long nodeId;
    private Long shipmentId;
    private String trackingNo;
    private String nodeStatus;
    private String title;
    private String description;
    private String location;
    private LocalDateTime eventTime;
    private LocalDateTime createTime;

    public Long getNodeId() { return nodeId; }
    public void setNodeId(Long nodeId) { this.nodeId = nodeId; }
    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getNodeStatus() { return nodeStatus; }
    public void setNodeStatus(String nodeStatus) { this.nodeStatus = nodeStatus; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getEventTime() { return eventTime; }
    public void setEventTime(LocalDateTime eventTime) { this.eventTime = eventTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
