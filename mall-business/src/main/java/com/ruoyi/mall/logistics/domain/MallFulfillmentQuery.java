package com.ruoyi.mall.logistics.domain;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class MallFulfillmentQuery
{
    private String orderNo;
    private String receiverKeyword;
    private String companyCode;
    private String workflowStatus;
    private String attentionType;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createEnd;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public String getReceiverKeyword() { return receiverKeyword; }
    public void setReceiverKeyword(String value) { receiverKeyword = value; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String value) { companyCode = value; }
    public String getWorkflowStatus() { return workflowStatus; }
    public void setWorkflowStatus(String value) { workflowStatus = value; }
    public String getAttentionType() { return attentionType; }
    public void setAttentionType(String value) { attentionType = value; }
    public LocalDateTime getCreateStart() { return createStart; }
    public void setCreateStart(LocalDateTime value) { createStart = value; }
    public LocalDateTime getCreateEnd() { return createEnd; }
    public void setCreateEnd(LocalDateTime value) { createEnd = value; }
}
