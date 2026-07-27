package com.ruoyi.mall.logistics.domain;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class MallLogisticsShipment implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long shipmentId;
    private Long orderId;
    private String orderNo;
    private Long memberId;
    private String companyCode;
    private String companyName;
    private String trackingNo;
    private String status;
    private LocalDateTime shippedTime;
    private LocalDateTime deliveredTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<MallLogisticsNode> nodes;

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getShippedTime() { return shippedTime; }
    public void setShippedTime(LocalDateTime shippedTime) { this.shippedTime = shippedTime; }
    public LocalDateTime getDeliveredTime() { return deliveredTime; }
    public void setDeliveredTime(LocalDateTime deliveredTime) { this.deliveredTime = deliveredTime; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    public List<MallLogisticsNode> getNodes() { return nodes; }
    public void setNodes(List<MallLogisticsNode> nodes) { this.nodes = nodes; }
}
