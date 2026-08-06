package com.ruoyi.mall.content.domain;

import java.time.LocalDateTime;
import java.util.List;
import com.ruoyi.common.core.domain.BaseEntity;

public class MallHomeContent extends BaseEntity
{
    private static final long serialVersionUID = 1L;
    private Long contentId;
    private String contentType;
    private String contentName;
    private String status;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private Integer sortNo;
    private String heroEyebrow;
    private String heroTitle;
    private String heroSubtitle;
    private String heroActionText;
    private String heroTargetType;
    private String heroTargetValue;
    private String heroPrimaryImage;
    private String heroSecondaryImage;
    private Integer heroNewItemCount;
    private String recommendationKicker;
    private String recommendationTitle;
    private String recommendationTargetType;
    private String recommendationTargetValue;
    private Integer recommendationDisplayCount;
    private String recommendationFallbackMode;
    private String runtimeStatus;
    private List<MallHomeTicker> tickerItems;
    private List<Long> recommendationSpuIds;

    public Long getContentId() { return contentId; }
    public void setContentId(Long value) { contentId = value; }
    public String getContentType() { return contentType; }
    public void setContentType(String value) { contentType = value; }
    public String getContentName() { return contentName; }
    public void setContentName(String value) { contentName = value; }
    public String getStatus() { return status; }
    public void setStatus(String value) { status = value; }
    public LocalDateTime getEffectiveTime() { return effectiveTime; }
    public void setEffectiveTime(LocalDateTime value) { effectiveTime = value; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime value) { expireTime = value; }
    public Integer getSortNo() { return sortNo; }
    public void setSortNo(Integer value) { sortNo = value; }
    public String getHeroEyebrow() { return heroEyebrow; }
    public void setHeroEyebrow(String value) { heroEyebrow = value; }
    public String getHeroTitle() { return heroTitle; }
    public void setHeroTitle(String value) { heroTitle = value; }
    public String getHeroSubtitle() { return heroSubtitle; }
    public void setHeroSubtitle(String value) { heroSubtitle = value; }
    public String getHeroActionText() { return heroActionText; }
    public void setHeroActionText(String value) { heroActionText = value; }
    public String getHeroTargetType() { return heroTargetType; }
    public void setHeroTargetType(String value) { heroTargetType = value; }
    public String getHeroTargetValue() { return heroTargetValue; }
    public void setHeroTargetValue(String value) { heroTargetValue = value; }
    public String getHeroPrimaryImage() { return heroPrimaryImage; }
    public void setHeroPrimaryImage(String value) { heroPrimaryImage = value; }
    public String getHeroSecondaryImage() { return heroSecondaryImage; }
    public void setHeroSecondaryImage(String value) { heroSecondaryImage = value; }
    public Integer getHeroNewItemCount() { return heroNewItemCount; }
    public void setHeroNewItemCount(Integer value) { heroNewItemCount = value; }
    public String getRecommendationKicker() { return recommendationKicker; }
    public void setRecommendationKicker(String value) { recommendationKicker = value; }
    public String getRecommendationTitle() { return recommendationTitle; }
    public void setRecommendationTitle(String value) { recommendationTitle = value; }
    public String getRecommendationTargetType() { return recommendationTargetType; }
    public void setRecommendationTargetType(String value) { recommendationTargetType = value; }
    public String getRecommendationTargetValue() { return recommendationTargetValue; }
    public void setRecommendationTargetValue(String value) { recommendationTargetValue = value; }
    public Integer getRecommendationDisplayCount() { return recommendationDisplayCount; }
    public void setRecommendationDisplayCount(Integer value) { recommendationDisplayCount = value; }
    public String getRecommendationFallbackMode() { return recommendationFallbackMode; }
    public void setRecommendationFallbackMode(String value) { recommendationFallbackMode = value; }
    public String getRuntimeStatus() { return runtimeStatus; }
    public void setRuntimeStatus(String value) { runtimeStatus = value; }
    public List<MallHomeTicker> getTickerItems() { return tickerItems; }
    public void setTickerItems(List<MallHomeTicker> value) { tickerItems = value; }
    public List<Long> getRecommendationSpuIds() { return recommendationSpuIds; }
    public void setRecommendationSpuIds(List<Long> value) { recommendationSpuIds = value; }
}
