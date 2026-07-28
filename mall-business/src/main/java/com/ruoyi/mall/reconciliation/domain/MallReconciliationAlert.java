package com.ruoyi.mall.reconciliation.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

public class MallReconciliationAlert implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long alertId; private String alertNo; private Long diffId; private String alertLevel;
    private String status; private String alertMessage; private String acknowledgedBy;
    private LocalDateTime acknowledgedTime; private LocalDateTime createTime; private LocalDateTime updateTime;
    public Long getAlertId(){return alertId;} public void setAlertId(Long v){alertId=v;}
    public String getAlertNo(){return alertNo;} public void setAlertNo(String v){alertNo=v;}
    public Long getDiffId(){return diffId;} public void setDiffId(Long v){diffId=v;}
    public String getAlertLevel(){return alertLevel;} public void setAlertLevel(String v){alertLevel=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getAlertMessage(){return alertMessage;} public void setAlertMessage(String v){alertMessage=v;}
    public String getAcknowledgedBy(){return acknowledgedBy;} public void setAcknowledgedBy(String v){acknowledgedBy=v;}
    public LocalDateTime getAcknowledgedTime(){return acknowledgedTime;} public void setAcknowledgedTime(LocalDateTime v){acknowledgedTime=v;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime v){createTime=v;}
    public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime v){updateTime=v;}
}
