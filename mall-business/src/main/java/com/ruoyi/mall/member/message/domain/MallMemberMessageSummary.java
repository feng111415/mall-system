package com.ruoyi.mall.member.message.domain;

import java.time.LocalDateTime;

public class MallMemberMessageSummary
{
    private String category;
    private String label;
    private String description;
    private Integer unreadCount;
    private Integer totalCount;
    private String latestTitle;
    private String latestSummary;
    private LocalDateTime latestTime;

    public MallMemberMessageSummary() { }
    public MallMemberMessageSummary(String category, String label, String description)
    { this.category = category; this.label = label; this.description = description; this.unreadCount = 0; this.totalCount = 0; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public String getLabel() { return label; }
    public void setLabel(String value) { label = value; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public Integer getUnreadCount() { return unreadCount; }
    public void setUnreadCount(Integer value) { unreadCount = value; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer value) { totalCount = value; }
    public String getLatestTitle() { return latestTitle; }
    public void setLatestTitle(String value) { latestTitle = value; }
    public String getLatestSummary() { return latestSummary; }
    public void setLatestSummary(String value) { latestSummary = value; }
    public LocalDateTime getLatestTime() { return latestTime; }
    public void setLatestTime(LocalDateTime value) { latestTime = value; }
}
