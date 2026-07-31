package com.ruoyi.mall.member.domain.vo;

import java.util.List;

public record MallMemberSessionOverviewVo(List<MallMemberSessionVo> sessions,
        int primaryChangesRemaining, int primaryChangeWindowDays)
{
}
