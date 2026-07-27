package com.ruoyi.mall.order.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.mall.member.domain.MallMemberAddress;

public class MallCheckoutPreview implements Serializable
{
    private static final long serialVersionUID = 1L;
    private List<MallCheckoutItem> items;
    private List<MallMemberAddress> addresses;
    private Long defaultAddressId;
    private BigDecimal productAmount;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
    private Boolean canSubmit;
    private List<String> validationMessages;

    public List<MallCheckoutItem> getItems() { return items; }
    public void setItems(List<MallCheckoutItem> items) { this.items = items; }
    public List<MallMemberAddress> getAddresses() { return addresses; }
    public void setAddresses(List<MallMemberAddress> addresses) { this.addresses = addresses; }
    public Long getDefaultAddressId() { return defaultAddressId; }
    public void setDefaultAddressId(Long defaultAddressId) { this.defaultAddressId = defaultAddressId; }
    public BigDecimal getProductAmount() { return productAmount; }
    public void setProductAmount(BigDecimal productAmount) { this.productAmount = productAmount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public void setPayableAmount(BigDecimal payableAmount) { this.payableAmount = payableAmount; }
    public Boolean getCanSubmit() { return canSubmit; }
    public void setCanSubmit(Boolean canSubmit) { this.canSubmit = canSubmit; }
    public List<String> getValidationMessages() { return validationMessages; }
    public void setValidationMessages(List<String> validationMessages) { this.validationMessages = validationMessages; }
}
