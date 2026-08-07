package com.ruoyi.mall.member.activity.domain;

import java.io.Serializable;

public class MallMemberProductActivitySummary implements Serializable
{
    private static final long serialVersionUID = 1L;
    private Integer favoriteCount;
    private Integer historyCount;

    public Integer getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Integer favoriteCount) { this.favoriteCount = favoriteCount; }
    public Integer getHistoryCount() { return historyCount; }
    public void setHistoryCount(Integer historyCount) { this.historyCount = historyCount; }
}
