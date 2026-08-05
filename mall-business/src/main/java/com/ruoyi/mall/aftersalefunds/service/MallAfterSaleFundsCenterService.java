package com.ruoyi.mall.aftersalefunds.service;

import java.util.List;
import java.util.Collection;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterQuery;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterView;
import com.ruoyi.mall.aftersalefunds.mapper.MallAfterSaleFundsCenterMapper;

@Service
public class MallAfterSaleFundsCenterService
{
    private static final Set<String> TABS = Set.of("ALL", "AFTER_SALE", "REFUND", "DIFF", "ALERT", "COMPENSATION");
    private final MallAfterSaleFundsCenterMapper mapper;

    public MallAfterSaleFundsCenterService(MallAfterSaleFundsCenterMapper mapper) { this.mapper = mapper; }

    public MallAfterSaleFundsCenterView center(MallAfterSaleFundsCenterQuery query, Collection<String> permissions)
    {
        MallAfterSaleFundsCenterQuery normalized = normalize(query);
        normalized.setAfterSaleVisible(hasPermission(permissions, "mall:after-sale:list"));
        normalized.setFinanceVisible(hasPermission(permissions, "mall:reconciliation:list")
                || hasPermission(permissions, "mall:compensation:list"));
        if (isFinanceTab(normalized.getTab()) && !normalized.isFinanceVisible())
            throw new ServiceException("无权查看资金异常工作队列");
        MallAfterSaleFundsCenterView view = new MallAfterSaleFundsCenterView();
        view.setSummary(mapper.selectSummary(normalized));
        view.setTotal(mapper.countWorkItems(normalized));
        view.setRows(mapper.selectWorkItems(normalized));
        view.setPageNum(normalized.getPageNum());
        view.setPageSize(normalized.getPageSize());
        return view;
    }

    private boolean hasPermission(Collection<String> permissions, String permission)
    {
        return permissions != null && SecurityUtils.hasPermi(permissions, permission);
    }

    private boolean isFinanceTab(String tab)
    {
        return List.of("DIFF", "ALERT", "COMPENSATION").contains(tab);
    }

    private MallAfterSaleFundsCenterQuery normalize(MallAfterSaleFundsCenterQuery source)
    {
        MallAfterSaleFundsCenterQuery query = source == null ? new MallAfterSaleFundsCenterQuery() : source;
        String tab = normalizeText(query.getTab());
        if (tab == null) tab = "ALL";
        tab = tab.toUpperCase();
        if (!TABS.contains(tab)) throw new ServiceException("工作队列类型无效");
        query.setTab(tab);
        query.setStatus(normalizeText(query.getStatus()));
        query.setBusinessNo(normalizeText(query.getBusinessNo()));
        query.setOrderNo(normalizeText(query.getOrderNo()));
        if (query.getFrom() != null && query.getTo() != null && query.getFrom().isAfter(query.getTo()))
            throw new ServiceException("时间范围无效");
        query.setPageNum(Math.max(1, query.getPageNum()));
        query.setPageSize(Math.min(Math.max(1, query.getPageSize()), 50));
        query.setOffset((query.getPageNum() - 1) * query.getPageSize());
        return query;
    }

    private String normalizeText(String value)
    {
        return StringUtils.isBlank(value) ? null : value.trim();
    }
}
