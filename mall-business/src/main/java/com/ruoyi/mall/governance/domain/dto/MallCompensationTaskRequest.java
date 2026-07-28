package com.ruoyi.mall.governance.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MallCompensationTaskRequest
{
    @NotBlank(message = "补偿类型不能为空")
    @Size(max = 32, message = "补偿类型长度不能超过32个字符")
    private String taskType;
    @NotBlank(message = "业务键不能为空")
    @Size(max = 128, message = "业务键长度不能超过128个字符")
    private String businessKey;
    @Size(max = 64, message = "订单号长度不能超过64个字符")
    private String orderNo;
    @Size(max = 2000, message = "任务载荷长度不能超过2000个字符")
    private String payload;

    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getBusinessKey() { return businessKey; }
    public void setBusinessKey(String businessKey) { this.businessKey = businessKey; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
}
