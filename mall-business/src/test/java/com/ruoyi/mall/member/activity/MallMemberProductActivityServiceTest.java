package com.ruoyi.mall.member.activity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.member.activity.domain.MallMemberProductActivityItem;
import com.ruoyi.mall.member.activity.mapper.MallMemberProductActivityMapper;
import com.ruoyi.mall.member.activity.service.MallMemberProductActivityService;

class MallMemberProductActivityServiceTest
{
    @Mock private MallMemberProductActivityMapper mapper;
    private MallMemberProductActivityService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallMemberProductActivityService(mapper);
    }

    @Test
    void addsPublishedProductToMemberFavoritesIdempotently()
    {
        MallMemberProductActivityItem product = availableProduct(2L);
        when(mapper.selectProductSnapshot(2L)).thenReturn(product);

        assertEquals(product, service.addFavorite(7L, 2L));
        verify(mapper).upsertFavorite(7L, product);
    }

    @Test
    void rejectsFavoriteWhenProductIsUnavailable()
    {
        MallMemberProductActivityItem product = availableProduct(2L);
        product.setAvailable(false);
        when(mapper.selectProductSnapshot(2L)).thenReturn(product);

        assertThrows(ServiceException.class, () -> service.addFavorite(7L, 2L));
        verify(mapper, never()).upsertFavorite(any(), any());
    }

    @Test
    void favoriteStateAndRemovalAreIsolatedByMember()
    {
        when(mapper.countFavorite(7L, 2L)).thenReturn(1);
        when(mapper.countFavorite(8L, 2L)).thenReturn(0);

        assertTrue(service.isFavorite(7L, 2L));
        assertFalse(service.isFavorite(8L, 2L));
        service.removeFavorite(7L, 2L);
        verify(mapper).deleteFavorite(7L, 2L);
    }

    @Test
    void repeatedProductVisitUsesHistoryUpsert()
    {
        MallMemberProductActivityItem product = availableProduct(2L);
        when(mapper.selectProductSnapshot(2L)).thenReturn(product);

        assertEquals(product, service.recordHistory(7L, 2L));
        verify(mapper).upsertHistory(7L, product);
    }

    @Test
    void batchDeleteDeduplicatesIdsAndKeepsMemberBoundary()
    {
        when(mapper.deleteHistoryBatch(eq(7L), any())).thenReturn(2);

        assertEquals(2, service.removeHistoryBatch(7L, List.of(2L, 2L, 3L)));
        verify(mapper).deleteHistoryBatch(7L, List.of(2L, 3L));
    }

    @Test
    void rejectsInvalidBatchDeleteAndDoesNotTouchMapper()
    {
        assertThrows(ServiceException.class, () -> service.removeHistoryBatch(7L, List.of(2L, -1L)));
        verify(mapper, never()).deleteHistoryBatch(any(), any());
    }

    @Test
    void clearHistoryOnlyUsesCurrentMember()
    {
        when(mapper.clearHistory(7L)).thenReturn(3);
        assertEquals(3, service.clearHistory(7L));
        verify(mapper).clearHistory(7L);
    }

    private MallMemberProductActivityItem availableProduct(Long spuId)
    {
        MallMemberProductActivityItem item = new MallMemberProductActivityItem();
        item.setSpuId(spuId); item.setProductName("Reading Lamp"); item.setMainImage("/assets/lamp.jpg");
        item.setPrice(new BigDecimal("299.00")); item.setPublishStatus("1"); item.setAvailable(true);
        return item;
    }
}
