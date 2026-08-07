package com.ruoyi.mall.member.activity.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.activity.domain.MallMemberProductActivityItem;
import com.ruoyi.mall.member.activity.domain.MallMemberProductActivitySummary;
import com.ruoyi.mall.member.activity.mapper.MallMemberProductActivityMapper;

@Service
public class MallMemberProductActivityService
{
    private final MallMemberProductActivityMapper mapper;

    public MallMemberProductActivityService(MallMemberProductActivityMapper mapper) { this.mapper = mapper; }

    public MallMemberProductActivitySummary summary(Long memberId)
    {
        requireMember(memberId);
        MallMemberProductActivitySummary summary = mapper.selectSummary(memberId);
        if (summary != null) return summary;
        summary = new MallMemberProductActivitySummary();
        summary.setFavoriteCount(0); summary.setHistoryCount(0);
        return summary;
    }

    public List<MallMemberProductActivityItem> favorites(Long memberId)
    { requireMember(memberId); return mapper.selectFavorites(memberId); }

    public List<MallMemberProductActivityItem> history(Long memberId)
    { requireMember(memberId); return mapper.selectHistory(memberId); }

    public boolean isFavorite(Long memberId, Long spuId)
    { requireMember(memberId); requireSpuId(spuId); return mapper.countFavorite(memberId, spuId) > 0; }

    @Transactional(rollbackFor = Exception.class)
    public MallMemberProductActivityItem addFavorite(Long memberId, Long spuId)
    {
        requireMember(memberId);
        MallMemberProductActivityItem item = requireAvailableProduct(spuId);
        mapper.upsertFavorite(memberId, item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public int removeFavorite(Long memberId, Long spuId)
    { requireMember(memberId); requireSpuId(spuId); return mapper.deleteFavorite(memberId, spuId); }

    @Transactional(rollbackFor = Exception.class)
    public MallMemberProductActivityItem recordHistory(Long memberId, Long spuId)
    {
        requireMember(memberId);
        MallMemberProductActivityItem item = requireAvailableProduct(spuId);
        mapper.upsertHistory(memberId, item);
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public int removeHistory(Long memberId, Long spuId)
    { requireMember(memberId); requireSpuId(spuId); return mapper.deleteHistory(memberId, spuId); }

    @Transactional(rollbackFor = Exception.class)
    public int removeHistoryBatch(Long memberId, List<Long> spuIds)
    {
        requireMember(memberId);
        if (spuIds == null || spuIds.isEmpty() || spuIds.size() > 100
                || spuIds.stream().anyMatch(value -> value == null || value <= 0))
            throw new ServiceException("浏览记录参数无效");
        return mapper.deleteHistoryBatch(memberId, spuIds.stream().distinct().toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public int clearHistory(Long memberId)
    { requireMember(memberId); return mapper.clearHistory(memberId); }

    private MallMemberProductActivityItem requireAvailableProduct(Long spuId)
    {
        requireSpuId(spuId);
        MallMemberProductActivityItem item = mapper.selectProductSnapshot(spuId);
        if (item == null || !Boolean.TRUE.equals(item.getAvailable()))
            throw new ServiceException("商品已下架或不存在");
        return item;
    }

    private void requireMember(Long memberId)
    { if (memberId == null || memberId <= 0) throw new ServiceException("请先登录"); }

    private void requireSpuId(Long spuId)
    { if (spuId == null || spuId <= 0) throw new ServiceException("商品参数无效"); }
}
