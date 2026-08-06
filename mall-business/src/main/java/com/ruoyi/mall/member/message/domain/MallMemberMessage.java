package com.ruoyi.mall.member.message.domain;

import java.time.LocalDateTime;

public class MallMemberMessage
{
    private Long messageId;
    private Long memberId;
    private String category;
    private String title;
    private String summary;
    private String content;
    private String businessType;
    private Long businessId;
    private String businessNo;
    private String actionPath;
    private String readFlag;
    private LocalDateTime readTime;
    private LocalDateTime createTime;
    private LocalDateTime expireTime;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long value) { messageId = value; }
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long value) { memberId = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { category = value; }
    public String getTitle() { return title; }
    public void setTitle(String value) { title = value; }
    public String getSummary() { return summary; }
    public void setSummary(String value) { summary = value; }
    public String getContent() { return content; }
    public void setContent(String value) { content = value; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String value) { businessType = value; }
    public Long getBusinessId() { return businessId; }
    public void setBusinessId(Long value) { businessId = value; }
    public String getBusinessNo() { return businessNo; }
    public void setBusinessNo(String value) { businessNo = value; }
    public String getActionPath() { return actionPath; }
    public void setActionPath(String value) { actionPath = value; }
    public String getReadFlag() { return readFlag; }
    public void setReadFlag(String value) { readFlag = value; }
    public LocalDateTime getReadTime() { return readTime; }
    public void setReadTime(LocalDateTime value) { readTime = value; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime value) { createTime = value; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime value) { expireTime = value; }
}
