package com.ruoyi.ticket.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务退款日志对象 ticket_refund_log
 *
 * @author ruoyi
 */
public class TicketRefundLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 退款日志ID */
    private Long refundId;

    /** 订单ID */
    @Excel(name = "订单ID")
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 商户退款单号 */
    @Excel(name = "商户退款单号")
    private String refundNo;

    /** 微信支付交易号 */
    @Excel(name = "微信支付交易号")
    private String transactionId;

    /** 微信退款单号 */
    @Excel(name = "微信退款单号")
    private String refundIdWx;

    /** 小程序用户ID */
    @Excel(name = "小程序用户ID")
    private Long userId;

    /** 退款金额 */
    @Excel(name = "退款金额")
    private BigDecimal refundAmount;

    /** 退款原因 */
    @Excel(name = "退款原因")
    private String refundReason;

    /** 退款状态 */
    @Excel(name = "退款状态")
    private String refundStatus;

    /** 回调时间 */
    @Excel(name = "回调时间")
    private Date notifyTime;

    /** 回调报文 */
    @Excel(name = "回调报文")
    private String notifyBody;

    public Long getRefundId()
    {
        return refundId;
    }

    public void setRefundId(Long refundId)
    {
        this.refundId = refundId;
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
    public String getRefundNo()
    {
        return refundNo;
    }

    public void setRefundNo(String refundNo)
    {
        this.refundNo = refundNo;
    }
    public String getTransactionId()
    {
        return transactionId;
    }

    public void setTransactionId(String transactionId)
    {
        this.transactionId = transactionId;
    }
    public String getRefundIdWx()
    {
        return refundIdWx;
    }

    public void setRefundIdWx(String refundIdWx)
    {
        this.refundIdWx = refundIdWx;
    }
    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }
    public BigDecimal getRefundAmount()
    {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount)
    {
        this.refundAmount = refundAmount;
    }
    public String getRefundReason()
    {
        return refundReason;
    }

    public void setRefundReason(String refundReason)
    {
        this.refundReason = refundReason;
    }
    public String getRefundStatus()
    {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus)
    {
        this.refundStatus = refundStatus;
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
            .append("refundId", getRefundId())
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("refundNo", getRefundNo())
            .append("transactionId", getTransactionId())
            .append("refundIdWx", getRefundIdWx())
            .append("userId", getUserId())
            .append("refundAmount", getRefundAmount())
            .append("refundReason", getRefundReason())
            .append("refundStatus", getRefundStatus())
            .append("notifyTime", getNotifyTime())
            .append("notifyBody", getNotifyBody())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
