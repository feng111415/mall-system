package com.ruoyi.ticket.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 活动对象 ticket_activity
 *
 * @author ruoyi
 */
public class TicketActivity extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 活动ID */
    private Long activityId;

    /** 活动名称 */
    @Excel(name = "活动名称")
    private String activityName;

    /** 活动编码 */
    @Excel(name = "活动编码")
    private String activityCode;

    /** 活动类型 */
    @Excel(name = "活动类型")
    private String activityType;

    /** 封面图 */
    @Excel(name = "封面图")
    private String coverUrl;

    /** 轮播图 */
    @Excel(name = "轮播图")
    private String bannerUrls;

    /** 场馆名称 */
    @Excel(name = "场馆名称")
    private String venueName;

    /** 场馆地址 */
    @Excel(name = "场馆地址")
    private String venueAddress;

    /** 经度 */
    @Excel(name = "经度")
    private BigDecimal longitude;

    /** 纬度 */
    @Excel(name = "纬度")
    private BigDecimal latitude;

    /** 开售时间 */
    @Excel(name = "开售时间")
    private Date saleStartTime;

    /** 停售时间 */
    @Excel(name = "停售时间")
    private Date saleEndTime;

    /** 活动开始时间 */
    @Excel(name = "活动开始时间")
    private Date startTime;

    /** 活动结束时间 */
    @Excel(name = "活动结束时间")
    private Date endTime;

    /** 每人限购数量 */
    @Excel(name = "每人限购数量")
    private Integer purchaseLimit;

    /** 是否实名 */
    @Excel(name = "是否实名")
    private String needRealName;

    /** 是否选座 */
    @Excel(name = "是否选座")
    private String needSeat;

    /** 订单支付超时秒数 */
    @Excel(name = "订单支付超时秒数")
    private Integer orderTimeout;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 删除标志 */
    @Excel(name = "删除标志")
    private String delFlag;

    /** 活动详情 */
    @Excel(name = "活动详情")
    private String description;

    public Long getActivityId()
    {
        return activityId;
    }

    public void setActivityId(Long activityId)
    {
        this.activityId = activityId;
    }
    public String getActivityName()
    {
        return activityName;
    }

    public void setActivityName(String activityName)
    {
        this.activityName = activityName;
    }
    public String getActivityCode()
    {
        return activityCode;
    }

    public void setActivityCode(String activityCode)
    {
        this.activityCode = activityCode;
    }
    public String getActivityType()
    {
        return activityType;
    }

    public void setActivityType(String activityType)
    {
        this.activityType = activityType;
    }
    public String getCoverUrl()
    {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl)
    {
        this.coverUrl = coverUrl;
    }
    public String getBannerUrls()
    {
        return bannerUrls;
    }

    public void setBannerUrls(String bannerUrls)
    {
        this.bannerUrls = bannerUrls;
    }
    public String getVenueName()
    {
        return venueName;
    }

    public void setVenueName(String venueName)
    {
        this.venueName = venueName;
    }
    public String getVenueAddress()
    {
        return venueAddress;
    }

    public void setVenueAddress(String venueAddress)
    {
        this.venueAddress = venueAddress;
    }
    public BigDecimal getLongitude()
    {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude)
    {
        this.longitude = longitude;
    }
    public BigDecimal getLatitude()
    {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude)
    {
        this.latitude = latitude;
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
    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }
    public Integer getPurchaseLimit()
    {
        return purchaseLimit;
    }

    public void setPurchaseLimit(Integer purchaseLimit)
    {
        this.purchaseLimit = purchaseLimit;
    }
    public String getNeedRealName()
    {
        return needRealName;
    }

    public void setNeedRealName(String needRealName)
    {
        this.needRealName = needRealName;
    }
    public String getNeedSeat()
    {
        return needSeat;
    }

    public void setNeedSeat(String needSeat)
    {
        this.needSeat = needSeat;
    }
    public Integer getOrderTimeout()
    {
        return orderTimeout;
    }

    public void setOrderTimeout(Integer orderTimeout)
    {
        this.orderTimeout = orderTimeout;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }
    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("activityId", getActivityId())
            .append("activityName", getActivityName())
            .append("activityCode", getActivityCode())
            .append("activityType", getActivityType())
            .append("coverUrl", getCoverUrl())
            .append("bannerUrls", getBannerUrls())
            .append("venueName", getVenueName())
            .append("venueAddress", getVenueAddress())
            .append("longitude", getLongitude())
            .append("latitude", getLatitude())
            .append("saleStartTime", getSaleStartTime())
            .append("saleEndTime", getSaleEndTime())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("purchaseLimit", getPurchaseLimit())
            .append("needRealName", getNeedRealName())
            .append("needSeat", getNeedSeat())
            .append("orderTimeout", getOrderTimeout())
            .append("status", getStatus())
            .append("delFlag", getDelFlag())
            .append("description", getDescription())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
