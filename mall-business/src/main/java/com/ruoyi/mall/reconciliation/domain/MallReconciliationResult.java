package com.ruoyi.mall.reconciliation.domain;

import java.math.BigDecimal;

public class MallReconciliationResult {
    private String diffType; private String businessNo; private String orderNo;
    private String externalStatus; private BigDecimal externalAmount;
    public String getDiffType(){return diffType;} public void setDiffType(String v){diffType=v;}
    public String getBusinessNo(){return businessNo;} public void setBusinessNo(String v){businessNo=v;}
    public String getOrderNo(){return orderNo;} public void setOrderNo(String v){orderNo=v;}
    public String getExternalStatus(){return externalStatus;} public void setExternalStatus(String v){externalStatus=v;}
    public BigDecimal getExternalAmount(){return externalAmount;} public void setExternalAmount(BigDecimal v){externalAmount=v;}
}
