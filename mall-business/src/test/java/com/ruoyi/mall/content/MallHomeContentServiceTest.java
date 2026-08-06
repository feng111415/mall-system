package com.ruoyi.mall.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.content.domain.MallHomeContent;
import com.ruoyi.mall.content.domain.MallHomeContentType;
import com.ruoyi.mall.content.domain.MallHomepageView;
import com.ruoyi.mall.content.mapper.MallHomeContentMapper;
import com.ruoyi.mall.content.service.impl.MallHomeContentServiceImpl;

class MallHomeContentServiceTest
{
    @Mock
    private MallHomeContentMapper mapper;

    private MallHomeContentServiceImpl service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallHomeContentServiceImpl(mapper);
    }

    @Test
    void publishedHomepageReturnsOneConfiguredSectionPerContentType()
    {
        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 9, 0);
        MallHomeContent hero = content(1L, MallHomeContentType.HERO, "八月上新主视觉");
        MallHomeContent ticker = content(2L, MallHomeContentType.TICKER, "配送公告条");
        MallHomeContent recommendation = content(3L, MallHomeContentType.RECOMMENDATION, "大家正在买");
        when(mapper.selectPublishedContentList(now)).thenReturn(List.of(hero, ticker, recommendation));

        MallHomepageView result = service.selectPublishedHomepage(now);

        assertEquals(hero, result.getHero());
        assertEquals(ticker, result.getTicker());
        assertEquals(recommendation, result.getRecommendation());
    }

    @Test
    void homepageFallsBackToEmptyViewWhenNoContentIsPublished()
    {
        LocalDateTime now = LocalDateTime.of(2026, 8, 6, 9, 0);
        when(mapper.selectPublishedContentList(now)).thenReturn(List.of());

        MallHomepageView result = service.selectPublishedHomepage(now);

        assertTrue(result.isEmpty());
    }

    @Test
    void rejectsOverlappingPublishedContentOfTheSameType()
    {
        MallHomeContent value = content(8L, MallHomeContentType.HERO, "重复主视觉");
        value.setStatus("PUBLISHED");
        value.setEffectiveTime(LocalDateTime.of(2026, 8, 6, 9, 0));
        value.setExpireTime(LocalDateTime.of(2026, 8, 7, 9, 0));
        value.setHeroTitle("新标题");
        value.setHeroPrimaryImage("/profile/hero-a.jpg");
        value.setHeroSecondaryImage("/profile/hero-b.jpg");
        when(mapper.countOverlappingPublished("HERO", value.getEffectiveTime(), value.getExpireTime(), 8L)).thenReturn(1);

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> service.saveContent(value));

        verify(mapper, never()).insertContent(value);
    }

    @Test
    void rejectsMoreThanFiveTickerItems()
    {
        MallHomeContent value = content(9L, MallHomeContentType.TICKER, "过多公告条");
        value.setEffectiveTime(LocalDateTime.of(2026, 8, 6, 9, 0));
        java.util.ArrayList<com.ruoyi.mall.content.domain.MallHomeTicker> items = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++)
        {
            com.ruoyi.mall.content.domain.MallHomeTicker item = new com.ruoyi.mall.content.domain.MallHomeTicker();
            item.setLabel("标签" + i); item.setText("公告" + i); items.add(item);
        }
        value.setTickerItems(items);

        assertThrows(RuntimeException.class, () -> service.saveContent(value));
        verify(mapper, never()).insertContent(value);
    }

    @Test
    void rejectsUnsafeInternalTarget()
    {
        MallHomeContent value = content(10L, MallHomeContentType.HERO, "非法跳转主视觉");
        value.setEffectiveTime(LocalDateTime.of(2026, 8, 6, 9, 0));
        value.setHeroTitle("主标题");
        value.setHeroPrimaryImage("/assets/sneaker.jpg");
        value.setHeroSecondaryImage("/profile/hero.jpg");
        value.setHeroTargetValue("javascript:alert(1)");

        assertThrows(RuntimeException.class, () -> service.saveContent(value));
        verify(mapper, never()).insertContent(value);
    }

    @Test
    void rejectsDuplicateRecommendationProducts()
    {
        MallHomeContent value = content(11L, MallHomeContentType.RECOMMENDATION, "重复推荐商品");
        value.setEffectiveTime(LocalDateTime.of(2026, 8, 6, 9, 0));
        value.setRecommendationSpuIds(List.of(2L, 2L));

        assertThrows(RuntimeException.class, () -> service.saveContent(value));
        verify(mapper, never()).insertContent(value);
    }

    private MallHomeContent content(Long id, MallHomeContentType type, String name)
    {
        MallHomeContent value = new MallHomeContent();
        value.setContentId(id);
        value.setContentType(type.name());
        value.setContentName(name);
        return value;
    }
}
