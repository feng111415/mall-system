package com.ruoyi.mall.analytics.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.mall.analytics.domain.MallAnalyticsMetric;
import com.ruoyi.mall.analytics.domain.MallBusinessAnalyticsView;
import com.ruoyi.mall.analytics.mapper.MallBusinessAnalyticsMapper;

@Service
public class MallBusinessAnalyticsService
{
    private static final Set<Integer> ALLOWED_DAYS = Set.of(7, 30, 90);
    private final MallBusinessAnalyticsMapper mapper;

    public MallBusinessAnalyticsService(MallBusinessAnalyticsMapper mapper)
    {
        this.mapper = mapper;
    }

    public MallBusinessAnalyticsView analytics(Integer requestedDays, boolean admin, Collection<String> roleKeys)
    {
        int days = requestedDays != null && ALLOWED_DAYS.contains(requestedDays) ? requestedDays : 30;
        LocalDate toDate = LocalDate.now();
        LocalDate fromDate = toDate.minusDays(days - 1L);
        LocalDateTime from = fromDate.atStartOfDay();
        LocalDateTime to = toDate.plusDays(1).atStartOfDay();
        Set<String> sections = resolveSections(admin, roleKeys);

        MallBusinessAnalyticsView view = new MallBusinessAnalyticsView();
        view.setFrom(fromDate);
        view.setTo(toDate);
        view.setSections(new ArrayList<>(sections));

        List<MallAnalyticsMetric> metrics = new ArrayList<>();
        BigDecimal sales = BigDecimal.ZERO;
        if (sections.contains("sales") || sections.contains("fulfillment") || sections.contains("finance"))
        {
            long orderCount = mapper.countOrders(from, to);
            metrics.add(count("orderCount", "订单量", orderCount, "单"));
            if (sections.contains("sales") || sections.contains("finance"))
            {
                sales = zero(mapper.sumSales(from, to));
                metrics.add(amount("salesAmount", "销售额", sales));
                view.setSalesTrend(mapper.selectSalesTrend(from, to));
            }
        }
        if (sections.contains("product"))
        {
            view.setProductRanking(mapper.selectProductRanking(from, to, 8));
            metrics.add(count("lowStockSkuCount", "低库存 SKU", mapper.countLowStockSkus(), "个"));
        }
        if (sections.contains("member"))
        {
            long paidMembers = mapper.countPaidMembers(from, to);
            long repeatMembers = mapper.countRepeatMembers(from, to);
            metrics.add(count("paidMemberCount", "成交会员", paidMembers, "人"));
            metrics.add(count("repeatMemberCount", "复购会员", repeatMembers, "人"));
            metrics.add(value("repeatRate", "会员复购率", percent(repeatMembers, paidMembers), "%"));
        }
        if (sections.contains("fulfillment"))
        {
            metrics.add(count("pendingShipment", "待发货", mapper.countPendingShipment(), "单"));
            metrics.add(count("inTransit", "运输中", mapper.countInTransitShipments(), "单"));
            metrics.add(count("delivered", "期间签收", mapper.countDeliveredShipments(from, to), "单"));
            metrics.add(count("availableStock", "可售库存", mapper.sumAvailableStock(), "件"));
            if (!sections.contains("product"))
            {
                metrics.add(count("lowStockSkuCount", "低库存 SKU", mapper.countLowStockSkus(), "个"));
            }
        }
        if (sections.contains("afterSale"))
        {
            metrics.add(count("afterSaleCount", "期间售后", mapper.countAfterSales(from, to), "单"));
            metrics.add(count("afterSalePending", "售后待处理", mapper.countAfterSalePending(), "单"));
        }
        if (sections.contains("finance"))
        {
            BigDecimal refunds = zero(mapper.sumRefunds(from, to));
            metrics.add(amount("refundAmount", "退款金额", refunds));
            metrics.add(amount("netRevenue", "净收入", sales.subtract(refunds)));
            metrics.add(count("financialAnomalies", "资金异常", mapper.countOpenFinancialAnomalies(), "项"));
        }
        view.setMetrics(metrics);
        return view;
    }

    Set<String> resolveSections(boolean admin, Collection<String> roleKeys)
    {
        Set<String> roles = roleKeys == null ? Set.of() : new LinkedHashSet<>(roleKeys);
        LinkedHashSet<String> sections = new LinkedHashSet<>();
        if (admin || roles.contains("mall_ops_lead"))
        {
            sections.addAll(List.of("sales", "product", "member", "fulfillment", "afterSale", "finance"));
            return sections;
        }
        if (roles.contains("mall_product_ops")) sections.addAll(List.of("sales", "product"));
        if (roles.contains("mall_customer_service")) sections.addAll(List.of("member", "afterSale"));
        if (roles.contains("mall_fulfillment")) sections.add("fulfillment");
        if (roles.contains("mall_finance_risk")) sections.addAll(List.of("sales", "finance"));
        return sections;
    }

    private MallAnalyticsMetric count(String key, String label, long count, String unit)
    {
        MallAnalyticsMetric metric = base(key, label, unit);
        metric.setCount(count);
        return metric;
    }

    private MallAnalyticsMetric amount(String key, String label, BigDecimal amount)
    {
        MallAnalyticsMetric metric = base(key, label, "元");
        metric.setAmount(zero(amount));
        return metric;
    }

    private MallAnalyticsMetric value(String key, String label, String value, String unit)
    {
        MallAnalyticsMetric metric = base(key, label, unit);
        metric.setValue(value);
        return metric;
    }

    private MallAnalyticsMetric base(String key, String label, String unit)
    {
        MallAnalyticsMetric metric = new MallAnalyticsMetric();
        metric.setKey(key);
        metric.setLabel(label);
        metric.setUnit(unit);
        return metric;
    }

    private BigDecimal zero(BigDecimal value)
    {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String percent(long numerator, long denominator)
    {
        if (denominator == 0) return "0.0";
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 1, RoundingMode.HALF_UP).toPlainString();
    }
}
