package com.ruoyi.ticket.domain;

import java.math.BigDecimal;
import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务订单对象 ticket_order
 *
 * @author ruoyi
 */
public class TicketOrder extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long orderId;

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

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

    /** 订单标题 */
    @Excel(name = "订单标题")
    private String orderTitle;

    /** 购票数量 */
    @Excel(name = "购票数量")
    private Integer totalQuantity;

    /** 订单总金额 */
    @Excel(name = "订单总金额")
    private BigDecimal totalAmount;

    /** 优惠金额 */
    @Excel(name = "优惠金额")
    private BigDecimal discountAmount;

    /** 实付金额 */
    @Excel(name = "实付金额")
    private BigDecimal payAmount;

    /** 支付状态 */
    @Excel(name = "支付状态")
    private String payStatus;

    /** 订单状态 */
    @Excel(name = "订单状态")
    private String orderStatus;

    /** 订单来源 */
    @Excel(name = "订单来源")
    private String source;

    /** 客户端IP */
    @Excel(name = "客户端IP")
    private String clientIp;

    /** 支付截止时间 */
    @Excel(name = "支付截止时间")
    private Date expireTime;

    /** 支付时间 */
    @Excel(name = "支付时间")
    private Date payTime;

    /** 取消时间 */
    @Excel(name = "取消时间")
    private Date cancelTime;

    /** 退款时间 */
    @Excel(name = "退款时间")
    private Date refundTime;

    /** 核销时间 */
    @Excel(name = "核销时间")
    private Date checkinTime;

    /** 微信支付交易号 */
    @Excel(name = "微信支付交易号")
    private String transactionId;

    /** 微信预支付ID */
    @Excel(name = "微信预支付ID")
    private String prepayId;

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
    public String getOrderTitle()
    {
        return orderTitle;
    }

    public void setOrderTitle(String orderTitle)
    {
        this.orderTitle = orderTitle;
    }
    public Integer getTotalQuantity()
    {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity)
    {
        this.totalQuantity = totalQuantity;
    }
    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }
    public BigDecimal getDiscountAmount()
    {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount)
    {
        this.discountAmount = discountAmount;
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
    public String getOrderStatus()
    {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus)
    {
        this.orderStatus = orderStatus;
    }
    public String getSource()
    {
        return source;
    }

    public void setSource(String source)
    {
        this.source = source;
    }
    public String getClientIp()
    {
        return clientIp;
    }

    public void setClientIp(String clientIp)
    {
        this.clientIp = clientIp;
    }
    public Date getExpireTime()
    {
        return expireTime;
    }

    public void setExpireTime(Date expireTime)
    {
        this.expireTime = expireTime;
    }
    public Date getPayTime()
    {
        return payTime;
    }

    public void setPayTime(Date payTime)
    {
        this.payTime = payTime;
    }
    public Date getCancelTime()
    {
        return cancelTime;
    }

    public void setCancelTime(Date cancelTime)
    {
        this.cancelTime = cancelTime;
    }
    public Date getRefundTime()
    {
        return refundTime;
    }

    public void setRefundTime(Date refundTime)
    {
        this.refundTime = refundTime;
    }
    public Date getCheckinTime()
    {
        return checkinTime;
    }

    public void setCheckinTime(Date checkinTime)
    {
        this.checkinTime = checkinTime;
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
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("orderId", getOrderId())
            .append("orderNo", getOrderNo())
            .append("userId", getUserId())
            .append("openid", getOpenid())
            .append("activityId", getActivityId())
            .append("sessionId", getSessionId())
            .append("orderTitle", getOrderTitle())
            .append("totalQuantity", getTotalQuantity())
            .append("totalAmount", getTotalAmount())
            .append("discountAmount", getDiscountAmount())
            .append("payAmount", getPayAmount())
            .append("payStatus", getPayStatus())
            .append("orderStatus", getOrderStatus())
            .append("source", getSource())
            .append("clientIp", getClientIp())
            .append("expireTime", getExpireTime())
            .append("payTime", getPayTime())
            .append("cancelTime", getCancelTime())
            .append("refundTime", getRefundTime())
            .append("checkinTime", getCheckinTime())
            .append("transactionId", getTransactionId())
            .append("prepayId", getPrepayId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
