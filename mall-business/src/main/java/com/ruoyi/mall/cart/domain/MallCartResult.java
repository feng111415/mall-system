package com.ruoyi.mall.cart.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class MallCartResult implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<MallCartItem> items;
    private Integer totalCount;
    private BigDecimal totalPrice;
    private Boolean canCheckout;

    public List<MallCartItem> getItems() { return items; }
    public void setItems(List<MallCartItem> items) { this.items = items; }
    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Boolean getCanCheckout() { return canCheckout; }
    public void setCanCheckout(Boolean canCheckout) { this.canCheckout = canCheckout; }
}
