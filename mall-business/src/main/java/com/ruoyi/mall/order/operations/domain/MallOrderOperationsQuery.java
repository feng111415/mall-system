package com.ruoyi.mall.order.operations.domain;

import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public class MallOrderOperationsQuery
{
    private String orderNo;
    private Long memberId;
    private String status;
    private String paymentStatus;
    private String riskStatus;
    private String logisticsStatus;
    private String afterSaleStatus;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createStart;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createEnd;

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String value) { paymentStatus = value; }
    public String getRiskStatus() { return riskStatus; }
    public void setRiskStatus(String value) { riskStatus = value; }
    public String getLogisticsStatus() { return logisticsStatus; }
    public void setLogisticsStatus(String value) { logisticsStatus = value; }
    public String getAfterSaleStatus() { return afterSaleStatus; }
    public void setAfterSaleStatus(String value) { afterSaleStatus = value; }
    public LocalDateTime getCreateStart() { return createStart; }
    public void setCreateStart(LocalDateTime value) { createStart = value; }
    public LocalDateTime getCreateEnd() { return createEnd; }
    public void setCreateEnd(LocalDateTime value) { createEnd = value; }
}
