package com.ruoyi.mall.reconciliation.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MallReconciliationDiff implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long diffId; private String diffNo; private String diffType; private String businessNo;
    private String orderNo; private String localStatus; private String externalStatus;
    private BigDecimal localAmount; private BigDecimal externalAmount; private String diffCode;
    private String diffMessage; private String status; private String handleRemark;
    private LocalDateTime createTime; private LocalDateTime updateTime; private LocalDateTime resolvedTime;
    public Long getDiffId(){return diffId;} public void setDiffId(Long v){diffId=v;}
    public String getDiffNo(){return diffNo;} public void setDiffNo(String v){diffNo=v;}
    public String getDiffType(){return diffType;} public void setDiffType(String v){diffType=v;}
    public String getBusinessNo(){return businessNo;} public void setBusinessNo(String v){businessNo=v;}
    public String getOrderNo(){return orderNo;} public void setOrderNo(String v){orderNo=v;}
    public String getLocalStatus(){return localStatus;} public void setLocalStatus(String v){localStatus=v;}
    public String getExternalStatus(){return externalStatus;} public void setExternalStatus(String v){externalStatus=v;}
    public BigDecimal getLocalAmount(){return localAmount;} public void setLocalAmount(BigDecimal v){localAmount=v;}
    public BigDecimal getExternalAmount(){return externalAmount;} public void setExternalAmount(BigDecimal v){externalAmount=v;}
    public String getDiffCode(){return diffCode;} public void setDiffCode(String v){diffCode=v;}
    public String getDiffMessage(){return diffMessage;} public void setDiffMessage(String v){diffMessage=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public String getHandleRemark(){return handleRemark;} public void setHandleRemark(String v){handleRemark=v;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime v){createTime=v;}
    public LocalDateTime getUpdateTime(){return updateTime;} public void setUpdateTime(LocalDateTime v){updateTime=v;}
    public LocalDateTime getResolvedTime(){return resolvedTime;} public void setResolvedTime(LocalDateTime v){resolvedTime=v;}
}
