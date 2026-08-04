package com.ruoyi.mall.order.operations.service;

import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.order.domain.MallOrderPaymentStatus;
import com.ruoyi.mall.order.domain.MallOrderStatus;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsDetail;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsQuery;
import com.ruoyi.mall.order.operations.domain.MallOrderOperationsSummary;
import com.ruoyi.mall.order.operations.mapper.MallOrderOperationsMapper;

@Service
public class MallOrderOperationsService
{
    private static final Set<String> RISK_STATUSES = Set.of("PENDING_CHECK", "PASSED", "REVIEW", "REJECTED");
    private static final Set<String> LOGISTICS_STATUSES = Set.of("IN_TRANSIT", "DELIVERED");
    private static final Set<String> AFTER_SALE_STATUSES = Set.of(
            "PENDING_REVIEW", "APPROVED", "RETURN_SHIPPED", "REFUNDING", "SUCCESS", "REJECTED", "FAILED");
    private final MallOrderOperationsMapper mapper;

    public MallOrderOperationsService(MallOrderOperationsMapper mapper) { this.mapper = mapper; }

    public List<MallOrderOperationsSummary> list(MallOrderOperationsQuery query)
    {
        MallOrderOperationsQuery normalized = query == null ? new MallOrderOperationsQuery() : query;
        normalize(normalized);
        return mapper.selectOrders(normalized);
    }

    public MallOrderOperationsDetail detail(Long orderId)
    {
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        MallOrderOperationsDetail detail = mapper.selectOrderDetail(orderId);
        if (detail == null) throw new ServiceException("订单不存在");
        detail.setItems(mapper.selectItems(orderId));
        detail.setPayments(mapper.selectPayments(orderId));
        detail.setReservations(mapper.selectReservations(detail.getOrderNo()));
        detail.setShipment(mapper.selectShipment(orderId));
        if (detail.getShipment() != null)
            detail.getShipment().setNodes(mapper.selectShipmentNodes(detail.getShipment().getShipmentId()));
        List<MallItemAfterSale> afterSales = mapper.selectAfterSales(orderId);
        afterSales.forEach(value -> value.setItems(mapper.selectAfterSaleItems(value.getAfterSaleId())));
        detail.setAfterSales(afterSales);
        detail.setRiskRecords(mapper.selectRiskRecords(orderId));
        detail.setOperations(mapper.selectOperations(orderId));
        return detail;
    }

    private void normalize(MallOrderOperationsQuery query)
    {
        query.setOrderNo(trim(query.getOrderNo()));
        if (query.getMemberId() != null && query.getMemberId() <= 0)
            throw new ServiceException("会员参数无效");
        query.setStatus(normalizeEnum(query.getStatus(), MallOrderStatus.class, "订单状态无效"));
        query.setPaymentStatus(normalizeEnum(query.getPaymentStatus(), MallOrderPaymentStatus.class, "支付状态无效"));
        query.setRiskStatus(normalizeSet(query.getRiskStatus(), RISK_STATUSES, "风控状态无效"));
        String logistics = normalizeSet(query.getLogisticsStatus(), LOGISTICS_STATUSES, "物流状态无效");
        query.setLogisticsStatus(logistics);
        String afterSale = trim(query.getAfterSaleStatus());
        if (afterSale != null)
        {
            afterSale = afterSale.toUpperCase();
            if (!"NONE".equals(afterSale) && !AFTER_SALE_STATUSES.contains(afterSale))
                throw new ServiceException("售后状态无效");
        }
        query.setAfterSaleStatus(afterSale);
        if (query.getCreateStart() != null && query.getCreateEnd() != null
                && query.getCreateStart().isAfter(query.getCreateEnd()))
            throw new ServiceException("创建时间范围无效");
    }

    private <E extends Enum<E>> String normalizeEnum(String value, Class<E> type, String message)
    {
        if (StringUtils.isBlank(value)) return null;
        String normalized = value.trim().toUpperCase();
        try { Enum.valueOf(type, normalized); return normalized; }
        catch (IllegalArgumentException exception) { throw new ServiceException(message); }
    }

    private String normalizeSet(String value, Set<String> allowed, String message)
    {
        if (StringUtils.isBlank(value)) return null;
        String normalized = value.trim().toUpperCase();
        if (!allowed.contains(normalized)) throw new ServiceException(message);
        return normalized;
    }

    private String trim(String value)
    {
        if (StringUtils.isBlank(value)) return null;
        return value.trim();
    }
}
