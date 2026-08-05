package com.ruoyi.mall.aftersalefunds.domain;

import java.io.Serializable;
import java.util.List;

public class MallAfterSaleFundsCenterView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private MallAfterSaleFundsSummary summary;
    private List<MallAfterSaleFundsWorkItem> rows;
    private long total;
    private int pageNum;
    private int pageSize;

    public MallAfterSaleFundsSummary getSummary() { return summary; }
    public void setSummary(MallAfterSaleFundsSummary value) { summary = value; }
    public List<MallAfterSaleFundsWorkItem> getRows() { return rows; }
    public void setRows(List<MallAfterSaleFundsWorkItem> value) { rows = value; }
    public long getTotal() { return total; }
    public void setTotal(long value) { total = value; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int value) { pageNum = value; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int value) { pageSize = value; }
}
