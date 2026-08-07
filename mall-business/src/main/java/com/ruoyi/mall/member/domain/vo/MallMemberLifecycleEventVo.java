package com.ruoyi.mall.member.domain.vo;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

public class MallMemberLifecycleEventVo
{
    private final String eventType;
    private final String title;
    private final String summary;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final Date eventTime;

    public MallMemberLifecycleEventVo(String eventType, String title, String summary, Date eventTime)
    {
        this.eventType = eventType;
        this.title = title;
        this.summary = summary;
        this.eventTime = eventTime;
    }

    public String getEventType() { return eventType; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public Date getEventTime() { return eventTime; }
}
