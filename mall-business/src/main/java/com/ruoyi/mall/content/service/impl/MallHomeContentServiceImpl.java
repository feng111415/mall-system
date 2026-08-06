package com.ruoyi.mall.content.service.impl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.content.domain.MallHomeContent;
import com.ruoyi.mall.content.domain.MallHomeContentQuery;
import com.ruoyi.mall.content.domain.MallHomeContentStatus;
import com.ruoyi.mall.content.domain.MallHomeContentType;
import com.ruoyi.mall.content.domain.MallHomeTicker;
import com.ruoyi.mall.content.domain.MallHomepageView;
import com.ruoyi.mall.content.mapper.MallHomeContentMapper;
import com.ruoyi.mall.content.service.IMallHomeContentService;

@Service
public class MallHomeContentServiceImpl implements IMallHomeContentService
{
    private final MallHomeContentMapper mapper;

    public MallHomeContentServiceImpl(MallHomeContentMapper mapper) { this.mapper = mapper; }

    @Override
    public MallHomepageView selectPublishedHomepage(LocalDateTime now)
    {
        LocalDateTime effectiveNow = now == null ? LocalDateTime.now() : now;
        MallHomepageView view = new MallHomepageView();
        for (MallHomeContent content : safe(mapper.selectPublishedContentList(effectiveNow)))
        {
            MallHomeContentType type = MallHomeContentType.parse(content.getContentType());
            if (type == MallHomeContentType.HERO && view.getHero() == null) view.setHero(content);
            if (type == MallHomeContentType.TICKER && view.getTicker() == null)
            {
                content.setTickerItems(safe(mapper.selectTickerItems(content.getContentId())));
                view.setTicker(content);
            }
            if (type == MallHomeContentType.RECOMMENDATION && view.getRecommendation() == null)
            {
                content.setRecommendationSpuIds(safe(mapper.selectRecommendationSpuIds(content.getContentId())));
                view.setRecommendation(content);
            }
        }
        return view;
    }

    @Override
    public List<MallHomeContent> selectAdminContentList(MallHomeContentQuery query)
    {
        MallHomeContentQuery normalized = query == null ? new MallHomeContentQuery() : query;
        if (StringUtils.isNotBlank(normalized.getContentType())) normalized.setContentType(MallHomeContentType.parse(normalized.getContentType()).name());
        if (StringUtils.isNotBlank(normalized.getStatus())) normalized.setStatus(MallHomeContentStatus.parse(normalized.getStatus()).name());
        if (StringUtils.isNotBlank(normalized.getKeyword())) normalized.setKeyword(normalized.getKeyword().trim());
        return mapper.selectAdminContentList(normalized);
    }

    @Override
    public MallHomeContent selectContentById(Long contentId)
    {
        if (contentId == null || contentId < 1) throw new ServiceException("首页内容编号不合法");
        MallHomeContent value = mapper.selectContentById(contentId);
        if (value == null) return null;
        if (MallHomeContentType.TICKER.name().equals(value.getContentType())) value.setTickerItems(safe(mapper.selectTickerItems(contentId)));
        if (MallHomeContentType.RECOMMENDATION.name().equals(value.getContentType())) value.setRecommendationSpuIds(safe(mapper.selectRecommendationSpuIds(contentId)));
        return value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveContent(MallHomeContent content)
    {
        validate(content);
        if (MallHomeContentStatus.PUBLISHED.name().equals(content.getStatus())
            && mapper.countOverlappingPublished(content.getContentType(), content.getEffectiveTime(), content.getExpireTime(), content.getContentId()) > 0)
            throw new ServiceException("同类型首页内容的生效时间存在重叠");
        int rows = content.getContentId() == null ? mapper.insertContent(content) : mapper.updateContent(content);
        if (content.getContentId() == null || rows < 1) throw new ServiceException("首页内容保存失败，请重试");
        if (MallHomeContentType.TICKER.name().equals(content.getContentType()))
        {
            mapper.deleteTickerItems(content.getContentId());
            int sort = 1;
            for (MallHomeTicker item : safe(content.getTickerItems()))
            {
                item.setContentId(content.getContentId());
                if (item.getSortNo() == null) item.setSortNo(sort++);
                mapper.insertTickerItem(item);
            }
        }
        if (MallHomeContentType.RECOMMENDATION.name().equals(content.getContentType()))
        {
            mapper.deleteRecommendationProducts(content.getContentId());
            int sort = 1;
            for (Long spuId : safe(content.getRecommendationSpuIds())) mapper.insertRecommendationProduct(content.getContentId(), spuId, sort++);
        }
        return rows;
    }

    @Override
    public int disableContent(Long contentId, String updateBy)
    {
        if (contentId == null || contentId < 1) throw new ServiceException("首页内容编号不合法");
        return mapper.disableContent(contentId, updateBy);
    }

    private void validate(MallHomeContent content)
    {
        if (content == null) throw new ServiceException("首页内容不能为空");
        MallHomeContentType type = MallHomeContentType.parse(content.getContentType());
        MallHomeContentStatus status = StringUtils.isBlank(content.getStatus())
            ? MallHomeContentStatus.DRAFT : MallHomeContentStatus.parse(content.getStatus());
        content.setContentType(type.name()); content.setStatus(status.name());
        if (StringUtils.isBlank(content.getContentName())) throw new ServiceException("首页内容名称不能为空");
        if (content.getEffectiveTime() == null) throw new ServiceException("生效时间不能为空");
        if (content.getSortNo() == null) content.setSortNo(0);
        if (content.getExpireTime() != null && !content.getExpireTime().isAfter(content.getEffectiveTime()))
            throw new ServiceException("下线时间必须晚于生效时间");
        if (type == MallHomeContentType.HERO)
        {
            if (StringUtils.isBlank(content.getHeroTitle())) throw new ServiceException("主视觉标题不能为空");
            if (StringUtils.isBlank(content.getHeroPrimaryImage()) || StringUtils.isBlank(content.getHeroSecondaryImage()))
                throw new ServiceException("主视觉图片不能为空");
            validateImagePath(content.getHeroPrimaryImage());
            validateImagePath(content.getHeroSecondaryImage());
            validateInternalTarget(content.getHeroTargetValue());
        }
        if (type == MallHomeContentType.TICKER)
        {
            List<MallHomeTicker> items = safe(content.getTickerItems());
            if (items.size() < 1 || items.size() > 5) throw new ServiceException("公告条数量必须为 1 至 5 组");
            for (MallHomeTicker item : items)
                if (StringUtils.isBlank(item.getLabel()) || StringUtils.isBlank(item.getText())) throw new ServiceException("公告条标签和内容不能为空");
        }
        if (type == MallHomeContentType.RECOMMENDATION)
        {
            int displayCount = content.getRecommendationDisplayCount() == null ? 4 : content.getRecommendationDisplayCount();
            if (displayCount < 1 || displayCount > 8) throw new ServiceException("推荐商品展示数量必须为 1 至 8");
            content.setRecommendationDisplayCount(displayCount);
            if (StringUtils.isBlank(content.getRecommendationFallbackMode())) content.setRecommendationFallbackMode("SALES");
            if (!"SALES".equals(content.getRecommendationFallbackMode())) throw new ServiceException("推荐商品补位规则不合法");
            validateInternalTarget(content.getRecommendationTargetValue());
            Set<Long> ids = new HashSet<>();
            for (Long spuId : safe(content.getRecommendationSpuIds()))
                if (spuId == null || spuId < 1 || !ids.add(spuId)) throw new ServiceException("推荐商品编号必须为不重复的正整数");
        }
        if (content.getRecommendationDisplayCount() == null) content.setRecommendationDisplayCount(4);
        if (StringUtils.isBlank(content.getRecommendationFallbackMode())) content.setRecommendationFallbackMode("SALES");
    }

    private void validateImagePath(String value)
    {
        String path = value == null ? "" : value.trim();
        if (!(path.startsWith("/profile/") || path.startsWith("/assets/")))
            throw new ServiceException("主视觉图片必须使用商城已上传资源");
    }

    private void validateInternalTarget(String value)
    {
        if (StringUtils.isBlank(value)) return;
        String target = value.trim();
        if (!(target.equals("/catalog") || target.startsWith("/catalog?") || target.matches("/product/[1-9][0-9]*")))
            throw new ServiceException("首页内容跳转目标不合法");
    }

    private static <T> List<T> safe(List<T> value) { return value == null ? Collections.emptyList() : value; }
}
