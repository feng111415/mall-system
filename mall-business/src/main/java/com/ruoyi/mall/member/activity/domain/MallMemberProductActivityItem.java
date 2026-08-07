package com.ruoyi.mall.member.activity.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MallMemberProductActivityItem implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Long spuId;
    private String productName;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private String publishStatus;
    private Boolean available;
    private LocalDateTime favoriteTime;
    private LocalDateTime lastViewTime;
    private Integer viewCount;

    public Long getSpuId() { return spuId; }
    public void setSpuId(Long spuId) { this.spuId = spuId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getMainImage() { return mainImage; }
    public void setMainImage(String mainImage) { this.mainImage = mainImage; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getPublishStatus() { return publishStatus; }
    public void setPublishStatus(String publishStatus) { this.publishStatus = publishStatus; }
    public Boolean getAvailable() { return available; }
    public void setAvailable(Boolean available) { this.available = available; }
    public LocalDateTime getFavoriteTime() { return favoriteTime; }
    public void setFavoriteTime(LocalDateTime favoriteTime) { this.favoriteTime = favoriteTime; }
    public LocalDateTime getLastViewTime() { return lastViewTime; }
    public void setLastViewTime(LocalDateTime lastViewTime) { this.lastViewTime = lastViewTime; }
    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }
}
