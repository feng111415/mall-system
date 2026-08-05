package com.ruoyi.mall.aftersalefunds.mapper;

import java.util.List;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterQuery;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsSummary;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsWorkItem;

public interface MallAfterSaleFundsCenterMapper
{
    MallAfterSaleFundsSummary selectSummary(MallAfterSaleFundsCenterQuery query);
    long countWorkItems(MallAfterSaleFundsCenterQuery query);
    List<MallAfterSaleFundsWorkItem> selectWorkItems(MallAfterSaleFundsCenterQuery query);
}
