package com.ruoyi.ticket.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务订单明细对象 ticket_order_item
 *
 * @author ruoyi
 */
public class TicketOrderItem extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单明细ID */
    private Long itemId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 小程序用户ID */
    @Excel(name = "小程序用户ID")
    private Long userId;

    /** 活动ID */
    @Excel(name = "活动ID")
    private Long activityId;

    /** 场次ID */
    @Excel(name = "场次ID")
    private Long sessionId;

    /** 票种ID */
    @Excel(name = "票种ID")
    private Long typeId;

    /** 票种名称 */
    @Excel(name = "票种名称")
    private String typeName;

    /** 票号 */
    @Excel(name = "票号")
    private String ticketNo;

    /** 购票人ID */
    @Excel(name = "购票人ID")
    private Long passengerId;

    /** 购票人姓名 */
    @Excel(name = "购票人姓名")
    private String passengerName;

    /** 证件类型 */
    @Excel(name = "证件类型")
    private String idType;

    /** 证件号码 */
    @Excel(name = "证件号码")
    private String idNo;

    /** 区域 */
    @Excel(name = "区域")
    private String seatArea;

    /** 排号 */
    @Excel(name = "排号")
    private String seatRow;

    /** 座位号 */
    @Excel(name = "座位号")
    private String seatNo;

    /** 单价 */
    @Excel(name = "单价")
    private BigDecimal price;

    /** 数量 */
    @Excel(name = "数量")
    private Integer quantity;

    /** 明细金额 */
    @Excel(name = "明细金额")
    private BigDecimal itemAmount;

    /** 明细状态 */
    @Excel(name = "明细状态")
    private String itemStatus;

    /** 核销时间 */
    @Excel(name = "核销时间")
    private Date checkinTime;

    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }
    public Long getOrderId()
    {
        return orderId;
    }

    public void setOrderId(Long orderId)
    {
        this.orderId = orderId;
    }
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
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
    public String getTypeName()
    {
        return typeName;
    }

    public void setTypeName(String typeName)
    {
        this.typeName = typeName;
    }
    public String getTicketNo()
    {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo)
    {
        this.ticketNo = ticketNo;
    }
    public Long getPassengerId()
    {
        return passengerId;
    }

    public void setPassengerId(Long passengerId)
    {
        this.passengerId = passengerId;
    }
    public String getPassengerName()
    {
        return passengerName;
    }

    public void setPassengerName(String passengerName)
    {
        this.passengerName = passengerName;
    }
    public String getIdType()
    {
        return idType;
    }

    public void setIdType(String idType)
    {
        this.idType = idType;
    }
    public String getIdNo()
    {
        return idNo;
    }

    public void setIdNo(String idNo)
    {
        this.idNo = idNo;
    }
    public String getSeatArea()
    {
        return seatArea;
    }

    public void setSeatArea(String seatArea)
    {
        this.seatArea = seatArea;
    }
    public String getSeatRow()
    {
        return seatRow;
    }

    public void setSeatRow(String seatRow)
    {
        this.seatRow = seatRow;
    }
    public String getSeatNo()
    {
        return seatNo;
    }

    public void setSeatNo(String seatNo)
    {
        this.seatNo = seatNo;
    }
    public BigDecimal getPrice()
    {
        return price;
    }

    public void setPrice(BigDecimal price)
    {
        this.price = price;
    }
    public Integer getQuantity()
    {
        return quantity;
    }

    public void setQuantity(Integer quantity)
    {
        this.quantity = quantity;
    }
    public BigDecimal getItemAmount()
    {
        return itemAmount;
    }

    public void setItemAmount(BigDecimal itemAmount)
    {
        this.itemAmount = itemAmount;
    }
    public String getItemStatus()
    {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus)
    {
        this.itemStatus = itemStatus;
    }
    public Date getCheckinTime()
    {
        return checkinTime;
    }

    public void setCheckinTime(Date checkinTime)
    {
        this.checkinTime = checkinTime;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("itemId", getItemId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("userId", getUserId())
            .append("activityId", getActivityId())
            .append("sessionId", getSessionId())
            .append("typeId", getTypeId())
            .append("typeName", getTypeName())
            .append("ticketNo", getTicketNo())
            .append("passengerId", getPassengerId())
            .append("passengerName", getPassengerName())
            .append("idType", getIdType())
            .append("idNo", getIdNo())
            .append("seatArea", getSeatArea())
            .append("seatRow", getSeatRow())
            .append("seatNo", getSeatNo())
            .append("price", getPrice())
            .append("quantity", getQuantity())
            .append("itemAmount", getItemAmount())
            .append("itemStatus", getItemStatus())
            .append("checkinTime", getCheckinTime())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
