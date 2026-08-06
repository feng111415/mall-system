package com.ruoyi.mall.content.domain;

import java.io.Serializable;

public class MallHomeTicker implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long tickerId;
    private Long contentId;
    private Integer sortNo;
    private String label;
    private String text;

    public Long getTickerId() { return tickerId; }
    public void setTickerId(Long value) { tickerId = value; }
    public Long getContentId() { return contentId; }
    public void setContentId(Long value) { contentId = value; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer value) { sortNo = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getText() { return text; }
    public void setText(String value) { text = value; }
}
