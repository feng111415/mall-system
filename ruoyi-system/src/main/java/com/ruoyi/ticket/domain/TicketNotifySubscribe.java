package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务订阅通知对象 ticket_notify_subscribe
 *
 * @author ruoyi
 */
public class TicketNotifySubscribe extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订阅ID */
    private Long subscribeId;

    /** 小程序用户ID */
    @Excel(name = "小程序用户ID")
    private Long userId;

    /** 微信openid */
    @Excel(name = "微信openid")
    private String openid;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 场次ID */
    @Excel(name = "场次ID")
    private Long sessionId;

    /** 订阅消息模板ID */
    @Excel(name = "订阅消息模板ID")
    private String templateId;

    /** 通知类型 */
    @Excel(name = "通知类型")
    private String notifyType;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 订阅时间 */
    @Excel(name = "订阅时间")
    private Date subscribeTime;

    /** 发送时间 */
    @Excel(name = "发送时间")
    private Date sendTime;

    /** 失败原因 */
    @Excel(name = "失败原因")
    private String failReason;

    public Long getSubscribeId()
    {
        return subscribeId;
    }

    public void setSubscribeId(Long subscribeId)
    {
        this.subscribeId = subscribeId;
    }
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    public String getOpenid()
    {
        return openid;
    }

    public void setOpenid(String openid)
    {
        this.openid = openid;
    }
    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }
    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }
    public String getTemplateId()
    {
        return templateId;
    }

    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }
    public String getNotifyType()
    {
        return notifyType;
    }

    public void setNotifyType(String notifyType)
    {
        this.notifyType = notifyType;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    public Date getSubscribeTime()
    {
        return subscribeTime;
    }

    public void setSubscribeTime(Date subscribeTime)
    {
        this.subscribeTime = subscribeTime;
    }
    public Date getSendTime()
    {
        return sendTime;
    }

    public void setSendTime(Date sendTime)
    {
        this.sendTime = sendTime;
    }
    public String getFailReason()
    {
        return failReason;
    }

    public void setFailReason(String failReason)
    {
        this.failReason = failReason;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("subscribeId", getSubscribeId())
            .append("userId", getUserId())
            .append("openid", getOpenid())
            .append("activityId", getActivityId())
            .append("sessionId", getSessionId())
            .append("templateId", getTemplateId())
            .append("notifyType", getNotifyType())
            .append("status", getStatus())
            .append("subscribeTime", getSubscribeTime())
            .append("sendTime", getSendTime())
            .append("failReason", getFailReason())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
