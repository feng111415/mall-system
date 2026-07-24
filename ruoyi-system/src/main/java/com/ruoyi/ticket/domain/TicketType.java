package com.ruoyi.ticket.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票种票档对象 ticket_type
 *
 * @author ruoyi
 */
public class TicketType extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 票种ID */
    private Long typeId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 场次ID */
    @Excel(name = "场次ID")
    private Long sessionId;

    /** 票种名称 */
    @Excel(name = "票种名称")
    private String typeName;

    /** 票种编码 */
    @Excel(name = "票种编码")
    private String typeCode;

    /** 票价 */
    @Excel(name = "票价")
    private BigDecimal price;

    /** 市场价 */
    @Excel(name = "市场价")
    private BigDecimal marketPrice;

    /** 总库存 */
    @Excel(name = "总库存")
    private Integer totalStock;

    /** 已售库存 */
    @Excel(name = "已售库存")
    private Integer soldStock;

    /** 锁定库存 */
    @Excel(name = "锁定库存")
    private Integer lockedStock;

    /** 最小购买数量 */
    @Excel(name = "最小购买数量")
    private Integer minBuy;

    /** 最大购买数量 */
    @Excel(name = "最大购买数量")
    private Integer maxBuy;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 排序 */
    @Excel(name = "排序")
    private Integer sort;

    public Long getTypeId()
    {
        return typeId;
    }

    public void setTypeId(Long typeId)
    {
        this.typeId = typeId;
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
    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
    public String getTypeCode()
    {
        return typeCode;
    }

    public void setTypeCode(String typeCode)
    {
        this.typeCode = typeCode;
    }
    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }
    public BigDecimal getMarketPrice()
    {
        return marketPrice;
    }

    public void setMarketPrice(BigDecimal marketPrice)
    {
        this.marketPrice = marketPrice;
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
    public Integer getMinBuy()
    {
        return minBuy;
    }

    public void setMinBuy(Integer minBuy)
    {
        this.minBuy = minBuy;
    }
    public Integer getMaxBuy()
    {
        return maxBuy;
    }

    public void setMaxBuy(Integer maxBuy)
    {
        this.maxBuy = maxBuy;
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
            .append("typeId", getTypeId())
            .append("activityId", getActivityId())
            .append("sessionId", getSessionId())
            .append("typeName", getTypeName())
            .append("typeCode", getTypeCode())
            .append("price", getPrice())
            .append("marketPrice", getMarketPrice())
            .append("totalStock", getTotalStock())
            .append("soldStock", getSoldStock())
            .append("lockedStock", getLockedStock())
            .append("minBuy", getMinBuy())
            .append("maxBuy", getMaxBuy())
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
