package com.ruoyi.mall.order.domain;

import java.util.EnumSet;
import java.util.Set;

/** Order state machine. Every state change must pass through canTransitionTo. */
public enum MallOrderStatus
{
    PENDING_PAYMENT,
    RISK_REVIEW,
    PENDING_SHIPMENT,
    SHIPPED,
    COMPLETED,
    AFTER_SALE,
    CANCELLED,
    CLOSED;

    public boolean canTransitionTo(MallOrderStatus target)
    {
        if (target == null || target == this) return false;
        return transitions(this).contains(target);
    }

    public boolean isTerminal()
    {
        return this == CANCELLED || this == CLOSED;
    }

    private static Set<MallOrderStatus> transitions(MallOrderStatus source)
    {
        return switch (source)
        {
            case PENDING_PAYMENT -> EnumSet.of(PENDING_SHIPMENT, RISK_REVIEW, CANCELLED, CLOSED);
            case RISK_REVIEW -> EnumSet.of(PENDING_PAYMENT, CLOSED);
            case PENDING_SHIPMENT -> EnumSet.of(SHIPPED, CANCELLED);
            case SHIPPED -> EnumSet.of(COMPLETED, AFTER_SALE);
            case COMPLETED -> EnumSet.of(AFTER_SALE);
            case AFTER_SALE, CANCELLED, CLOSED -> EnumSet.noneOf(MallOrderStatus.class);
        };
    }
}
