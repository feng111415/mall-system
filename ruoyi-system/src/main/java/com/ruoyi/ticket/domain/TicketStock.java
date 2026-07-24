package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务库存对象 ticket_stock
 *
 * @author ruoyi
 */
public class TicketStock extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 库存ID */
    private Long stockId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 场次ID */
    @Excel(name = "场次ID")
    private Long sessionId;

    /** 票种ID */
    @Excel(name = "票种ID")
    private Long typeId;

    /** 总库存 */
    @Excel(name = "总库存")
    private Integer totalStock;

    /** 可售库存 */
    @Excel(name = "可售库存")
    private Integer availableStock;

    /** 锁定库存 */
    @Excel(name = "锁定库存")
    private Integer lockedStock;

    /** 已售库存 */
    @Excel(name = "已售库存")
    private Integer soldStock;

    /** 乐观锁版本 */
    @Excel(name = "乐观锁版本")
    private Integer version;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    public Long getStockId()
    {
        return stockId;
    }

    public void setStockId(Long stockId)
    {
        this.stockId = stockId;
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
    public Long getTypeId()
    {
        return typeId;
    }

    public void setTypeId(Long typeId)
    {
        this.typeId = typeId;
    }
    public Integer getTotalStock()
    {
        return totalStock;
    }

    public void setTotalStock(Integer totalStock)
    {
        this.totalStock = totalStock;
    }
    public Integer getAvailableStock()
    {
        return availableStock;
    }

    public void setAvailableStock(Integer availableStock)
    {
        this.availableStock = availableStock;
    }
    public Integer getLockedStock()
    {
        return lockedStock;
    }

    public void setLockedStock(Integer lockedStock)
    {
        this.lockedStock = lockedStock;
    }
    public Integer getSoldStock()
    {
        return soldStock;
    }

    public void setSoldStock(Integer soldStock)
    {
        this.soldStock = soldStock;
    }
    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("stockId", getStockId())
            .append("activityId", getActivityId())
            .append("sessionId", getSessionId())
            .append("typeId", getTypeId())
            .append("totalStock", getTotalStock())
            .append("availableStock", getAvailableStock())
            .append("lockedStock", getLockedStock())
            .append("soldStock", getSoldStock())
            .append("version", getVersion())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
