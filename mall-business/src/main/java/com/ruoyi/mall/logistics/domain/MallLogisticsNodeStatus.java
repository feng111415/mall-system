package com.ruoyi.mall.logistics.domain;

import com.ruoyi.common.exception.ServiceException;

/** 物流轨迹允许的追加式节点状态。 */
public enum MallLogisticsNodeStatus
{
    SHIPPED("已发货"), IN_TRANSIT("运输中"), OUT_FOR_DELIVERY("派送中"), ARRIVED("已送达"),
    DELIVERED("已签收"), EXCEPTION("运输异常"), CORRECTION("更正说明");

    private final String label;

    MallLogisticsNodeStatus(String label) { this.label = label; }

    public String getLabel() { return label; }

    public static MallLogisticsNodeStatus parse(String value)
    {
        if (value == null || value.isBlank()) throw new ServiceException("物流节点状态不能为空");
        try { return valueOf(value.trim().toUpperCase()); }
        catch (IllegalArgumentException exception) { throw new ServiceException("物流节点状态无效"); }
    }
}
