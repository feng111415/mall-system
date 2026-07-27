package com.ruoyi.mall.payment.domain.dto;

public class MallCreatePaymentRequest
{
    private String idempotencyKey;

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
