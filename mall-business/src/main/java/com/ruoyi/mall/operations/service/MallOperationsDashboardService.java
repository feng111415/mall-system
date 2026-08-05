package com.ruoyi.mall.operations.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsCenterQuery;
import com.ruoyi.mall.aftersalefunds.domain.MallAfterSaleFundsSummary;
import com.ruoyi.mall.aftersalefunds.service.MallAfterSaleFundsCenterService;
import com.ruoyi.mall.order.operations.mapper.MallOrderOperationsMapper;
import com.ruoyi.mall.operations.domain.MallOperationsDashboardMetric;
import com.ruoyi.mall.operations.domain.MallOperationsDashboardView;
import com.ruoyi.mall.productinventory.mapper.MallProductInventoryMapper;

@Service
public class MallOperationsDashboardService
{
    private final MallOrderOperationsMapper orderMapper;
    private final MallProductInventoryMapper inventoryMapper;
    private final MallAfterSaleFundsCenterService fundsService;

    public MallOperationsDashboardService(MallOrderOperationsMapper orderMapper,
            MallProductInventoryMapper inventoryMapper, MallAfterSaleFundsCenterService fundsService)
    {
        this.orderMapper = orderMapper;
        this.inventoryMapper = inventoryMapper;
        this.fundsService = fundsService;
    }

    public MallOperationsDashboardView dashboard(Collection<String> permissions)
    {
        Collection<String> safePermissions = permissions == null ? List.of() : permissions;
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        List<MallOperationsDashboardMetric> metrics = new ArrayList<>();

        if (has(safePermissions, "mall:order-center:list"))
        {
            metrics.add(count("todayOrders", "今日订单", "创建于今日", "normal", "/mall/order-fulfillment/order-center",
                    orderMapper.countOrdersSince(start)));
            metrics.add(amount("todaySales", "今日销售额", "已支付订单应付金额", "normal", "/mall/order-fulfillment/order-center",
                    orderMapper.sumPaidAmountSince(start)));
            metrics.add(count("pendingPayment", "待付款", "等待会员完成支付", "warning", "/mall/order-fulfillment/order-center",
                    orderMapper.countOrdersByStatus("PENDING_PAYMENT")));
        }
        if (has(safePermissions, "mall:logistics:list"))
        {
            metrics.add(count("pendingShipment", "待发货", "已支付待创建物流单", "warning", "/mall/order-fulfillment/logistics",
                    orderMapper.countOrdersByStatus("PENDING_SHIPMENT")));
        }
        if (has(safePermissions, "mall:product-inventory:list"))
        {
            metrics.add(count("lowStock", "低库存预警", "可售库存已达到预警阈值", "danger", "/mall/product-inventory/product-inventory-center",
                    inventoryMapper.countLowStockSkus()));
        }

        MallAfterSaleFundsSummary funds = fundsService.center(new MallAfterSaleFundsCenterQuery(), safePermissions).getSummary();
        if (has(safePermissions, "mall:after-sale:list"))
        {
            metrics.add(count("afterSalePending", "售后待审核", "等待客服审核的订单项售后", "warning", "/mall/after-sale-funds/after-sale",
                    funds.getPendingReview()));
            metrics.add(count("refundPending", "退款处理中", "已审核但尚未完成退款", "warning", "/mall/after-sale-funds/after-sale",
                    funds.getRefundPending()));
            metrics.add(count("refundFailed", "退款失败", "需要财务风控介入", "danger", "/mall/after-sale-funds/after-sale",
                    funds.getRefundFailed()));
        }
        if (has(safePermissions, "mall:reconciliation:list") || has(safePermissions, "mall:compensation:list"))
        {
            metrics.add(count("openDiffs", "对账差异", "尚未解决的支付/退款差异", "danger", "/mall/after-sale-funds/reconciliation",
                    funds.getOpenDiffs()));
            metrics.add(count("openAlerts", "资金告警", "尚未确认的异常告警", "danger", "/mall/after-sale-funds/reconciliation",
                    funds.getOpenAlerts()));
            metrics.add(count("manualCompensations", "待执行补偿", "需要人工执行的补偿任务", "danger", "/mall/after-sale-funds/center",
                    funds.getManualCompensations()));
        }

        MallOperationsDashboardView view = new MallOperationsDashboardView();
        view.setAsOfDate(today);
        view.setMetrics(metrics);
        return view;
    }

    private boolean has(Collection<String> permissions, String permission)
    {
        return SecurityUtils.hasPermi(permissions, permission);
    }

    private MallOperationsDashboardMetric count(String key, String label, String hint, String severity,
            String path, long value)
    {
        MallOperationsDashboardMetric metric = base(key, label, hint, severity, path);
        metric.setValueType("COUNT");
        metric.setCount(value);
        return metric;
    }

    private MallOperationsDashboardMetric amount(String key, String label, String hint, String severity,
            String path, BigDecimal value)
    {
        MallOperationsDashboardMetric metric = base(key, label, hint, severity, path);
        metric.setValueType("AMOUNT");
        metric.setAmount(value == null ? BigDecimal.ZERO : value);
        return metric;
    }

    private MallOperationsDashboardMetric base(String key, String label, String hint, String severity, String path)
    {
        MallOperationsDashboardMetric metric = new MallOperationsDashboardMetric();
        metric.setKey(key); metric.setLabel(label); metric.setHint(hint); metric.setSeverity(severity); metric.setPath(path);
        return metric;
    }
}
