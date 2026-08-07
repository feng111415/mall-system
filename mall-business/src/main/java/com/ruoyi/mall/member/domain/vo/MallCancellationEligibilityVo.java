package com.ruoyi.mall.member.domain.vo;

public class MallCancellationEligibilityVo
{
    private final int unfinishedOrderCount;
    private final int inProgressAfterSaleCount;

    public MallCancellationEligibilityVo(int unfinishedOrderCount, int inProgressAfterSaleCount)
    {
        this.unfinishedOrderCount = unfinishedOrderCount;
        this.inProgressAfterSaleCount = inProgressAfterSaleCount;
    }

    public int getUnfinishedOrderCount() { return unfinishedOrderCount; }
    public int getInProgressAfterSaleCount() { return inProgressAfterSaleCount; }
    public boolean isEligible() { return unfinishedOrderCount == 0 && inProgressAfterSaleCount == 0; }
}
