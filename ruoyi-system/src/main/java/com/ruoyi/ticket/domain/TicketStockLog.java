package com.ruoyi.ticket.domain;

import java.util.Date;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 票务库存流水对象 ticket_stock_log
 *
 * @author ruoyi
 */
public class TicketStockLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 库存流水ID */
    private Long logId;

    /** 库存ID */
    @Excel(name = "库存ID")
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

    /** 订单号 */
    @Excel(name = "订单号")
    private String orderNo;

    /** 变更类型 */
    @Excel(name = "变更类型")
    private String changeType;

    /** 变更数量 */
    @Excel(name = "变更数量")
    private Integer changeQty;

    /** 变更前可售库存 */
    @Excel(name = "变更前可售库存")
    private Integer beforeQty;

    /** 变更后可售库存 */
    @Excel(name = "变更后可售库存")
    private Integer afterQty;

    /** 操作人ID */
    @Excel(name = "操作人ID")
    private Long operatorId;

    /** 操作人名称 */
    @Excel(name = "操作人名称")
    private String operatorName;

    public Long getLogId()
    {
        return logId;
    }

    public void setLogId(Long logId)
    {
        this.logId = logId;
    }
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
    public String getOrderNo()
    {
        return orderNo;
    }

    public void setOrderNo(String orderNo)
    {
        this.orderNo = orderNo;
    }
    public String getChangeType()
    {
        return changeType;
    }

    public void setChangeType(String changeType)
    {
        this.changeType = changeType;
    }
    public Integer getChangeQty()
    {
        return changeQty;
    }

    public void setChangeQty(Integer changeQty)
    {
        this.changeQty = changeQty;
    }
    public Integer getBeforeQty()
    {
        return beforeQty;
    }

    public void setBeforeQty(Integer beforeQty)
    {
        this.beforeQty = beforeQty;
    }
    public Integer getAfterQty()
    {
        return afterQty;
    }

    public void setAfterQty(Integer afterQty)
    {
        this.afterQty = afterQty;
    }
    public Long getOperatorId()
    {
        return operatorId;
    }

    public void setOperatorId(Long operatorId)
    {
        this.operatorId = operatorId;
    }
    public String getOperatorName()
    {
        return operatorName;
    }

    public void setOperatorName(String operatorName)
    {
        this.operatorName = operatorName;
    }
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("stockId", getStockId())
            .append("activityId", getActivityId())
            .append("sessionId", getSessionId())
            .append("typeId", getTypeId())
            .append("orderNo", getOrderNo())
            .append("changeType", getChangeType())
            .append("changeQty", getChangeQty())
            .append("beforeQty", getBeforeQty())
            .append("afterQty", getAfterQty())
            .append("operatorId", getOperatorId())
            .append("operatorName", getOperatorName())
            .append("createTime", getCreateTime())
            .append("remark", getRemark())
            .toString();
    }
}
