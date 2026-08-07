package com.ruoyi.mall.member.activity.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.member.activity.domain.MallMemberProductActivityItem;
import com.ruoyi.mall.member.activity.domain.MallMemberProductActivitySummary;

public interface MallMemberProductActivityMapper
{
    MallMemberProductActivityItem selectProductSnapshot(@Param("spuId") Long spuId);
    MallMemberProductActivitySummary selectSummary(@Param("memberId") Long memberId);
    List<MallMemberProductActivityItem> selectFavorites(@Param("memberId") Long memberId);
    List<MallMemberProductActivityItem> selectHistory(@Param("memberId") Long memberId);
    int countFavorite(@Param("memberId") Long memberId, @Param("spuId") Long spuId);
    int upsertFavorite(@Param("memberId") Long memberId, @Param("item") MallMemberProductActivityItem item);
    int deleteFavorite(@Param("memberId") Long memberId, @Param("spuId") Long spuId);
    int upsertHistory(@Param("memberId") Long memberId, @Param("item") MallMemberProductActivityItem item);
    int deleteHistory(@Param("memberId") Long memberId, @Param("spuId") Long spuId);
    int deleteHistoryBatch(@Param("memberId") Long memberId, @Param("spuIds") List<Long> spuIds);
    int clearHistory(@Param("memberId") Long memberId);
}
