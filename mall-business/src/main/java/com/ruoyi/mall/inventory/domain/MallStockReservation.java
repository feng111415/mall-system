package com.ruoyi.mall.inventory.domain;

import java.io.Serializable;

public class MallStockReservation implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long reservationId;
    private String orderNo;
    private Long skuId;
    private Integer quantity;
    private String status;

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public Long getSkuId() { return skuId; }
    public void setSkuId(Long skuId) { this.skuId = skuId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
