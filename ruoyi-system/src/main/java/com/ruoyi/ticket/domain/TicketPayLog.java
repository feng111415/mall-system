package com.ruoyi.ticket.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务支付日志对象 ticket_pay_log
 *
 * @author ruoyi
 */
public class TicketPayLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 支付日志ID */
    private Long payId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 支付流水号 */
    @Excel(name = "支付流水号")
    private String payNo;

    /** 小程序用户ID */
    @Excel(name = "小程序用户ID")
    private Long userId;

    /** 微信openid */
    @Excel(name = "微信openid")
    private String openid;

    /** 支付渠道 */
    @Excel(name = "支付渠道")
    private String payChannel;

    /** 支付金额 */
    @Excel(name = "支付金额")
    private BigDecimal payAmount;

    /** 支付状态 */
    @Excel(name = "支付状态")
    private String payStatus;

    /** 微信支付交易号 */
    @Excel(name = "微信支付交易号")
    private String transactionId;

    /** 微信预支付ID */
    @Excel(name = "微信预支付ID")
    private String prepayId;

    /** 回调时间 */
    @Excel(name = "回调时间")
    private Date notifyTime;

    /** 回调报文 */
    @Excel(name = "回调报文")
    private String notifyBody;

    public Long getPayId()
    {
        return payId;
    }

    public void setPayId(Long payId)
    {
        this.payId = payId;
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
    public String getPayNo()
    {
        return payNo;
    }

    public void setPayNo(String payNo)
    {
        this.payNo = payNo;
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
    public String getPayChannel()
    {
        return payChannel;
    }

    public void setPayChannel(String payChannel)
    {
        this.payChannel = payChannel;
    }
    public BigDecimal getPayAmount()
    {
        return payAmount;
    }

    public void setPayAmount(BigDecimal payAmount)
    {
        this.payAmount = payAmount;
    }
    public String getPayStatus()
    {
        return payStatus;
    }

    public void setPayStatus(String payStatus)
    {
        this.payStatus = payStatus;
    }
    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }
    public String getPrepayId()
    {
        return prepayId;
    }

    public void setPrepayId(String prepayId)
    {
        this.prepayId = prepayId;
    }
    public Date getNotifyTime()
    {
        return notifyTime;
    }

    public void setNotifyTime(Date notifyTime)
    {
        this.notifyTime = notifyTime;
    }
    public String getNotifyBody()
    {
        return notifyBody;
    }

    public void setNotifyBody(String notifyBody)
    {
        this.notifyBody = notifyBody;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("payId", getPayId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("payNo", getPayNo())
            .append("userId", getUserId())
            .append("openid", getOpenid())
            .append("payChannel", getPayChannel())
            .append("payAmount", getPayAmount())
            .append("payStatus", getPayStatus())
            .append("transactionId", getTransactionId())
            .append("prepayId", getPrepayId())
            .append("notifyTime", getNotifyTime())
            .append("notifyBody", getNotifyBody())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
