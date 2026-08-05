package com.ruoyi.mall.aftersalefunds.domain;

import java.time.LocalDateTime;

public class MallAfterSaleFundsCenterQuery
{
    private String tab;
    private String status;
    private String businessNo;
    private String orderNo;
    private LocalDateTime from;
    private LocalDateTime to;
    private int pageNum = 1;
    private int pageSize = 10;
    private int offset;
    private boolean afterSaleVisible;
    private boolean financeVisible;

    public String getTab() { return tab; }
    public void setTab(String value) { tab = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String value) { businessNo = value; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String value) { orderNo = value; }
    public LocalDateTime getFrom() { return from; }
    public void setFrom(LocalDateTime value) { from = value; }
    public LocalDateTime getTo() { return to; }
    public void setTo(LocalDateTime value) { to = value; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int value) { pageNum = value; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int value) { pageSize = value; }
    public int getOffset() { return offset; }
    public void setOffset(int value) { offset = value; }
    public boolean isAfterSaleVisible() { return afterSaleVisible; }
    public void setAfterSaleVisible(boolean value) { afterSaleVisible = value; }
    public boolean isFinanceVisible() { return financeVisible; }
    public void setFinanceVisible(boolean value) { financeVisible = value; }
}
