package com.ruoyi.mall.order;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.ruoyi.mall.order.domain.MallOrderStatus;

class MallOrderStatusTest
{
    @Test
    void pendingPaymentCanMoveToShipmentAfterPayment()
    {
        assertTrue(MallOrderStatus.PENDING_PAYMENT.canTransitionTo(MallOrderStatus.PENDING_SHIPMENT));
    }

    @Test
    void pendingPaymentCanBeCancelledOrClosed()
    {
        assertTrue(MallOrderStatus.PENDING_PAYMENT.canTransitionTo(MallOrderStatus.CANCELLED));
        assertTrue(MallOrderStatus.PENDING_PAYMENT.canTransitionTo(MallOrderStatus.CLOSED));
    }

    @Test
    void orderCannotSkipShipmentOrReopenAfterClosed()
    {
        assertFalse(MallOrderStatus.PENDING_PAYMENT.canTransitionTo(MallOrderStatus.COMPLETED));
        assertFalse(MallOrderStatus.CLOSED.canTransitionTo(MallOrderStatus.PENDING_PAYMENT));
        assertFalse(MallOrderStatus.CANCELLED.canTransitionTo(MallOrderStatus.PENDING_SHIPMENT));
    }
}
