package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务核销码对象 ticket_checkin_code
 *
 * @author ruoyi
 */
public class TicketCheckinCode extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 核销码ID */
    private Long codeId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 订单明细ID */
    @Excel(name = "订单明细ID")
    private Long itemId;

    /** 票号 */
    @Excel(name = "票号")
    private String ticketNo;

    /** 核销码 */
    @Excel(name = "核销码")
    private String checkinCode;

    /** 二维码地址 */
    @Excel(name = "二维码地址")
    private String qrcodeUrl;

    /** 状态 */
    @Excel(name = "状态")
    private String status;

    /** 核销人ID */
    @Excel(name = "核销人ID")
    private Long checkinUserId;

    /** 核销人 */
    @Excel(name = "核销人")
    private String checkinUser;

    /** 核销时间 */
    @Excel(name = "核销时间")
    private Date checkinTime;

    public Long getCodeId()
    {
        return codeId;
    }

    public void setCodeId(Long codeId)
    {
        this.codeId = codeId;
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
    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long itemId)
    {
        this.itemId = itemId;
    }
    public String getTicketNo()
    {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo)
    {
        this.ticketNo = ticketNo;
    }
    public String getCheckinCode()
    {
        return checkinCode;
    }

    public void setCheckinCode(String checkinCode)
    {
        this.checkinCode = checkinCode;
    }
    public String getQrcodeUrl()
    {
        return qrcodeUrl;
    }

    public void setQrcodeUrl(String qrcodeUrl)
    {
        this.qrcodeUrl = qrcodeUrl;
    }
    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }
    public Long getCheckinUserId()
    {
        return checkinUserId;
    }

    public void setCheckinUserId(Long checkinUserId)
    {
        this.checkinUserId = checkinUserId;
    }
    public String getCheckinUser()
    {
        return checkinUser;
    }

    public void setCheckinUser(String checkinUser)
    {
        this.checkinUser = checkinUser;
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
            .append("codeId", getCodeId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("itemId", getItemId())
            .append("ticketNo", getTicketNo())
            .append("checkinCode", getCheckinCode())
            .append("qrcodeUrl", getQrcodeUrl())
            .append("status", getStatus())
            .append("checkinUserId", getCheckinUserId())
            .append("checkinUser", getCheckinUser())
            .append("checkinTime", getCheckinTime())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
