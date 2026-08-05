package com.ruoyi.mall.aftersalefunds;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterQuery;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsSummary;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsWorkItem;
import com.ruoyi.mall.aftersalefunds.mapper.MallAfterSaleFundsCenterMapper;
import com.ruoyi.mall.aftersalefunds.service.MallAfterSaleFundsCenterService;

class MallAfterSaleFundsCenterServiceTest
{
    @Mock private MallAfterSaleFundsCenterMapper mapper;
    private MallAfterSaleFundsCenterService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallAfterSaleFundsCenterService(mapper);
    }

    @Test
    void centerNormalizesFiltersAndPaging()
    {
        MallAfterSaleFundsCenterQuery query = new MallAfterSaleFundsCenterQuery();
        query.setTab(" refund "); query.setBusinessNo(" AS-1 "); query.setPageNum(2); query.setPageSize(100);
        when(mapper.selectSummary(query)).thenReturn(new MallAfterSaleFundsSummary());
        when(mapper.countWorkItems(query)).thenReturn(1L);
        when(mapper.selectWorkItems(query)).thenReturn(List.of(new MallAfterSaleFundsWorkItem()));

        assertEquals(1L, service.center(query, List.of("mall:after-sale:list", "mall:reconciliation:list")).getTotal());
        assertEquals("REFUND", query.getTab());
        assertEquals("AS-1", query.getBusinessNo());
        assertEquals(50, query.getPageSize());
        assertEquals(50, query.getOffset());
        verify(mapper).selectWorkItems(query);
    }

    @Test
    void centerRejectsUnknownTabAndReversedTime()
    {
        MallAfterSaleFundsCenterQuery invalidTab = new MallAfterSaleFundsCenterQuery();
        invalidTab.setTab("RISK");
        assertThrows(ServiceException.class, () -> service.center(invalidTab, List.of("*:*:*")));

        MallAfterSaleFundsCenterQuery invalidTime = new MallAfterSaleFundsCenterQuery();
        invalidTime.setFrom(java.time.LocalDateTime.of(2026, 8, 5, 12, 0));
        invalidTime.setTo(java.time.LocalDateTime.of(2026, 8, 5, 11, 0));
        assertThrows(ServiceException.class, () -> service.center(invalidTime, List.of("*:*:*")));
    }

    @Test
    void customerCannotOpenFinanceQueue()
    {
        MallAfterSaleFundsCenterQuery query = new MallAfterSaleFundsCenterQuery();
        query.setTab("DIFF");
        assertThrows(ServiceException.class, () -> service.center(query, List.of("mall:after-sale:list")));
    }
}
