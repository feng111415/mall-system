package com.ruoyi.mall.application.port;

/** Boundary used by order/payment flows; callers do not access inventory tables. */
public interface InventoryPort
{
    void reserve(String orderNo, Long skuId, int quantity);
    void release(String orderNo);
    /** Called by the payment-success transaction. */
    void confirm(String orderNo);
    /** Explicit form used when the caller has a payment status value to validate. */
    void confirm(String orderNo, boolean paymentConfirmed);
}
