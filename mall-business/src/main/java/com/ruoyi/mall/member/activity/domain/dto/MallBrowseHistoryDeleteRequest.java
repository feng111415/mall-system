package com.ruoyi.mall.member.activity.domain.dto;

import java.util.List;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class MallBrowseHistoryDeleteRequest
{
    @NotEmpty(message = "请选择要删除的浏览记录")
    @Size(max = 100, message = "单次最多删除100条浏览记录")
    private List<Long> spuIds;

    public List<Long> getSpuIds() { return spuIds; }
    public void setSpuIds(List<Long> spuIds) { this.spuIds = spuIds; }
}
