package com.ruoyi.mall.content.domain;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class MallHomepageView implements Serializable
{
    private static final long serialVersionUID = 1L;
    private MallHomeContent hero;
    private MallHomeContent ticker;
    private MallHomeContent recommendation;

    public MallHomeContent getHero() { return hero; }
    public void setHero(MallHomeContent value) { hero = value; }
    public MallHomeContent getTicker() { return ticker; }
    public void setTicker(MallHomeContent value) { ticker = value; }
    public MallHomeContent getRecommendation() { return recommendation; }
    public void setRecommendation(MallHomeContent value) { recommendation = value; }
    public boolean isEmpty() { return hero == null && ticker == null && recommendation == null; }
    public List<MallHomeTicker> getTickerItems()
    {
        return ticker == null || ticker.getTickerItems() == null ? Collections.emptyList() : ticker.getTickerItems();
    }
    public List<Long> getRecommendationSpuIds()
    {
        return recommendation == null || recommendation.getRecommendationSpuIds() == null
            ? Collections.emptyList() : recommendation.getRecommendationSpuIds();
    }
}
