package com.ruoyi.mall.aftersale.item.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class MallItemAfterSaleItemRequest
{
    @NotNull(message = "订单项不能为空")
    private Long orderItemId;
    @NotNull(message = "售后数量不能为空")
    @Positive(message = "售后数量必须大于 0")
    private Integer quantity;

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long value) { this.orderItemId = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { this.quantity = value; }
}
