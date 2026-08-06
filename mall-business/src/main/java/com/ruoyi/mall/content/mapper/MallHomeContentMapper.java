package com.ruoyi.mall.content.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mall.content.domain.MallHomeContent;
import com.ruoyi.mall.content.domain.MallHomeContentQuery;
import com.ruoyi.mall.content.domain.MallHomeTicker;

public interface MallHomeContentMapper
{
    List<MallHomeContent> selectPublishedContentList(LocalDateTime now);
    List<MallHomeContent> selectAdminContentList(MallHomeContentQuery query);
    MallHomeContent selectContentById(Long contentId);
    List<MallHomeTicker> selectTickerItems(Long contentId);
    List<Long> selectRecommendationSpuIds(Long contentId);
    int countOverlappingPublished(@Param("contentType") String contentType, @Param("effectiveTime") LocalDateTime effectiveTime,
        @Param("expireTime") LocalDateTime expireTime, @Param("contentId") Long contentId);
    int insertContent(MallHomeContent content);
    int updateContent(MallHomeContent content);
    int deleteTickerItems(Long contentId);
    int insertTickerItem(MallHomeTicker item);
    int deleteRecommendationProducts(Long contentId);
    int insertRecommendationProduct(@Param("contentId") Long contentId, @Param("spuId") Long spuId, @Param("sortNo") Integer sortNo);
    int disableContent(@Param("contentId") Long contentId, @Param("updateBy") String updateBy);
}
