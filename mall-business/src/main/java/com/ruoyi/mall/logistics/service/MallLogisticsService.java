package com.ruoyi.mall.logistics.service;

import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.LogisticsPort;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.logistics.mapper.MallLogisticsMapper;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;

@Service
public class MallLogisticsService
{
    private static final String DEFAULT_COMPANY_CODE = "MOCK";
    private static final String DEFAULT_COMPANY_NAME = "模拟物流";
    private final MallLogisticsMapper logisticsMapper;
    private final MallOrderMapper orderMapper;
    private final LogisticsPort logisticsPort;

    public MallLogisticsService(MallLogisticsMapper logisticsMapper, MallOrderMapper orderMapper,
            LogisticsPort logisticsPort)
    {
        this.logisticsMapper = logisticsMapper;
        this.orderMapper = orderMapper;
        this.logisticsPort = logisticsPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallLogisticsShipment shipByAdmin(Long orderId, String companyCode, String operatorId)
    {
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        MallOrder order = orderMapper.selectByIdForUpdate(orderId, null);
        if (order == null) throw new ServiceException("订单不存在");
        MallLogisticsShipment existing = logisticsMapper.selectByOrderIdForUpdate(orderId);
        if (existing != null) return withNodes(existing);
        if (!"PENDING_SHIPMENT".equals(order.getStatus()) || !"PAID".equals(order.getPaymentStatus()))
            throw new ServiceException("当前订单不允许发货");

        String normalizedCompany = StringUtils.isBlank(companyCode) ? DEFAULT_COMPANY_CODE
                : companyCode.trim().toUpperCase();
        LogisticsPort.LogisticsCreateResult result = logisticsPort.createShipment(order.getOrderNo(),
                normalizedCompany, order.getReceiverName(), fullAddress(order));
        if (result == null || !result.success() || StringUtils.isBlank(result.trackingNo()))
            throw new ServiceException(result == null ? "物流渠道未返回结果" : result.message());

        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setOrderId(order.getOrderId()); shipment.setOrderNo(order.getOrderNo());
        shipment.setMemberId(order.getMemberId()); shipment.setCompanyCode(normalizedCompany);
        shipment.setCompanyName(companyName(normalizedCompany)); shipment.setTrackingNo(result.trackingNo());
        shipment.setStatus("IN_TRANSIT"); shipment.setShippedTime(LocalDateTime.now());
        if (logisticsMapper.insertShipment(shipment) != 1) throw new ServiceException("物流单保存失败");

        if (orderMapper.markShipped(order.getOrderId()) != 1)
            throw new ServiceException("订单状态已变化，请刷新后重试");

        MallLogisticsNode node = new MallLogisticsNode();
        node.setShipmentId(shipment.getShipmentId()); node.setTrackingNo(shipment.getTrackingNo());
        node.setNodeStatus("SHIPPED"); node.setTitle("商家已发货");
        node.setDescription("商家已创建物流运单，包裹等待揽收"); node.setLocation("商家仓库");
        node.setEventTime(shipment.getShippedTime());
        if (logisticsMapper.insertNode(node) != 1) throw new ServiceException("物流轨迹保存失败");

        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(order.getOrderId()); log.setOrderNo(order.getOrderNo());
        log.setFromStatus("PENDING_SHIPMENT"); log.setToStatus("SHIPPED");
        log.setOperatorType("ADMIN"); log.setOperatorId(normalizeOperator(operatorId));
        log.setRemark("后台发货，物流公司：" + normalizedCompany);
        log.setRequestId(shipment.getTrackingNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        shipment.setNodes(java.util.List.of(node));
        return shipment;
    }

    public MallLogisticsShipment detailForMember(Long memberId, Long orderId)
    {
        if (memberId == null || memberId <= 0 || orderId == null || orderId <= 0)
            throw new ServiceException("物流查询参数无效");
        MallLogisticsShipment shipment = logisticsMapper.selectMemberShipment(orderId, memberId);
        if (shipment == null) throw new ServiceException("物流单不存在");
        return withNodes(shipment);
    }

    private MallLogisticsShipment withNodes(MallLogisticsShipment shipment)
    {
        shipment.setNodes(logisticsMapper.selectNodes(shipment.getShipmentId()));
        return shipment;
    }

    private String fullAddress(MallOrder order)
    {
        return String.join(" ", safe(order.getReceiverProvince()), safe(order.getReceiverCity()),
                safe(order.getReceiverDistrict()), safe(order.getReceiverDetailAddress()));
    }

    private String companyName(String code)
    {
        return DEFAULT_COMPANY_CODE.equals(code) ? DEFAULT_COMPANY_NAME : code;
    }

    private String normalizeOperator(String operatorId)
    {
        return StringUtils.isBlank(operatorId) ? "system" : operatorId.length() > 64
                ? operatorId.substring(0, 64) : operatorId;
    }

    private String safe(String value) { return value == null ? "" : value; }
}
