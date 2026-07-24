package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 活动场次对象 ticket_session
 *
 * @author ruoyi
 */
public class TicketSession extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 场次ID */
    private Long sessionId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 场次名称 */
    @Excel(name = "场次名称")
    private String sessionName;

    /** 场次编码 */
    @Excel(name = "场次编码")
    private String sessionCode;

    /** 场次开始时间 */
    @Excel(name = "场次开始时间")
    private Date sessionStart;

    /** 场次结束时间 */
    @Excel(name = "场次结束时间")
    private Date sessionEnd;

    /** 场次开售时间 */
    @Excel(name = "场次开售时间")
    private Date saleStartTime;

    /** 场次停售时间 */
    @Excel(name = "场次停售时间")
    private Date saleEndTime;

    /** 场馆名称 */
    @Excel(name = "场馆名称")
    private String venueName;

    /** 总库存 */
    @Excel(name = "总库存")
    private Integer totalStock;

    /** 已售库存 */
    @Excel(name = "已售库存")
    private Integer soldStock;

    /** 锁定库存 */
    @Excel(name = "锁定库存")
    private Integer lockedStock;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sort;

    public Long getSessionId()
    {
        return sessionId;
    }

    public void setSessionId(Long sessionId)
    {
        this.sessionId = sessionId;
    }
    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }
    public String getSessionName()
    {
        return sessionName;
    }

    public void setSessionName(String sessionName)
    {
        this.sessionName = sessionName;
    }
    public String getSessionCode()
    {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode)
    {
        this.sessionCode = sessionCode;
    }
    public Date getSessionStart()
    {
        return sessionStart;
    }

    public void setSessionStart(Date sessionStart)
    {
        this.sessionStart = sessionStart;
    }
    public Date getSessionEnd()
    {
        return sessionEnd;
    }

    public void setSessionEnd(Date sessionEnd)
    {
        this.sessionEnd = sessionEnd;
    }
    public Date getSaleStartTime()
    {
        return saleStartTime;
    }

    public void setSaleStartTime(Date saleStartTime)
    {
        this.saleStartTime = saleStartTime;
    }
    public Date getSaleEndTime()
    {
        return saleEndTime;
    }

    public void setSaleEndTime(Date saleEndTime)
    {
        this.saleEndTime = saleEndTime;
    }
    public String getVenueName()
    {
        return venueName;
    }

    public void setVenueName(String venueName)
    {
        this.venueName = venueName;
    }
    public Integer getTotalStock()
    {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock)
    {
        this.totalStock = totalStock;
    }
    public Integer getSoldStock()
    {
        return soldStock;
    }

    public void setSoldStock(Integer soldStock)
    {
        this.soldStock = soldStock;
    }
    public Integer getLockedStock()
    {
        return lockedStock;
    }

    public void setLockedStock(Integer lockedStock)
    {
        this.lockedStock = lockedStock;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    public Integer getSort()
    {
        return sort;
    }

    public void setSort(Integer sort)
    {
        this.sort = sort;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("sessionId", getSessionId())
            .append("activityId", getActivityId())
            .append("sessionName", getSessionName())
            .append("sessionCode", getSessionCode())
            .append("sessionStart", getSessionStart())
            .append("sessionEnd", getSessionEnd())
            .append("saleStartTime", getSaleStartTime())
            .append("saleEndTime", getSaleEndTime())
            .append("venueName", getVenueName())
            .append("totalStock", getTotalStock())
            .append("soldStock", getSoldStock())
            .append("lockedStock", getLockedStock())
            .append("status", getStatus())
            .append("sort", getSort())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
