package com.ruoyi.mall.logistics.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MallLogisticsNodeRequest
{
    @NotBlank(message = "物流节点状态不能为空")
    private String nodeStatus;
    @NotBlank(message = "物流节点标题不能为空")
    @Size(max = 100, message = "物流节点标题长度不能超过100个字符")
    private String title;
    @NotBlank(message = "物流节点说明不能为空")
    @Size(max = 500, message = "物流节点说明长度不能超过500个字符")
    private String description;
    @Size(max = 100, message = "物流节点地点长度不能超过100个字符")
    private String location;
    private String eventTime;

    public String getNodeStatus() { return nodeStatus; }
    public void setNodeStatus(String nodeStatus) { this.nodeStatus = nodeStatus; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEventTime() { return eventTime; }
    public void setEventTime(String eventTime) { this.eventTime = eventTime; }
}
