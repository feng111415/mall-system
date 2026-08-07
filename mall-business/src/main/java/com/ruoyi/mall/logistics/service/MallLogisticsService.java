package com.ruoyi.mall.logistics.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.LogisticsPort;
import com.ruoyi.mall.application.port.MemberMessagePort;
import com.ruoyi.mall.logistics.domain.MallLogisticsNode;
import com.ruoyi.mall.logistics.domain.MallLogisticsShipment;
import com.ruoyi.mall.logistics.domain.MallLogisticsNodeStatus;
import com.ruoyi.mall.logistics.domain.MallFulfillmentOrder;
import com.ruoyi.mall.logistics.domain.MallLogisticsCompany;
import com.ruoyi.mall.logistics.domain.MallLogisticsCompanyOption;
import com.ruoyi.mall.logistics.domain.MallFulfillmentQuery;
import com.ruoyi.mall.logistics.domain.MallFulfillmentSummary;
import com.ruoyi.mall.logistics.domain.dto.MallLogisticsNodeRequest;
import com.ruoyi.mall.logistics.mapper.MallLogisticsMapper;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class MallLogisticsService
{
    private static final Set<String> WORKFLOW_STATUSES = Set.of("WAITING_SHIPMENT", "IN_TRANSIT",
            "OUT_FOR_DELIVERY", "ARRIVED", "EXCEPTION", "DELIVERED_WAIT_RECEIPT", "COMPLETED", "DATA_INCONSISTENT");
    private static final Set<String> ATTENTION_TYPES = Set.of("OVERDUE_UNSHIPPED", "TRANSPORT_EXCEPTION",
            "DATA_INCONSISTENT");
    private final MallLogisticsMapper logisticsMapper;
    private final MallOrderMapper orderMapper;
    private final LogisticsPort logisticsPort;
    private final MemberMessagePort memberMessagePort;

    public MallLogisticsService(MallLogisticsMapper logisticsMapper, MallOrderMapper orderMapper,
            LogisticsPort logisticsPort)
    { this(logisticsMapper, orderMapper, logisticsPort, null); }

    @Autowired
    public MallLogisticsService(MallLogisticsMapper logisticsMapper, MallOrderMapper orderMapper,
            LogisticsPort logisticsPort, MemberMessagePort memberMessagePort)
    {
        this.logisticsMapper = logisticsMapper;
        this.orderMapper = orderMapper;
        this.logisticsPort = logisticsPort;
        this.memberMessagePort = memberMessagePort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallLogisticsShipment shipByAdmin(Long orderId, String companyCode, String companyName,
            String trackingNo, String operatorId)
    {
        if (orderId == null || orderId <= 0) throw new ServiceException("订单参数无效");
        MallOrder order = orderMapper.selectByIdForUpdate(orderId, null);
        if (order == null) throw new ServiceException("订单不存在");
        MallLogisticsShipment existing = logisticsMapper.selectByOrderIdForUpdate(orderId);
        if (existing != null) return withNodes(existing);
        if (!"PENDING_SHIPMENT".equals(order.getStatus()) || !"PAID".equals(order.getPaymentStatus()))
            throw new ServiceException("当前订单不允许发货");

        MallLogisticsCompany company = MallLogisticsCompany.parse(companyCode);
        String normalizedCompany = company.getCode();
        String normalizedTracking = trackingNo.trim();
        LogisticsPort.LogisticsCreateResult result = logisticsPort.createShipment(order.getOrderNo(),
                normalizedCompany, normalizedTracking, order.getReceiverName(), fullAddress(order));
        if (result == null || !result.success() || StringUtils.isBlank(result.trackingNo()))
            throw new ServiceException(result == null ? "物流渠道未返回结果" : result.message());

        MallLogisticsShipment shipment = new MallLogisticsShipment();
        shipment.setOrderId(order.getOrderId()); shipment.setOrderNo(order.getOrderNo());
        shipment.setMemberId(order.getMemberId()); shipment.setCompanyCode(normalizedCompany);
        shipment.setCompanyName(company.getName()); shipment.setTrackingNo(result.trackingNo());
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
        publishLogisticsMessage(shipment, "物流已发货",
                "订单 " + shipment.getOrderNo() + " 已发货，运单号 " + shipment.getTrackingNo());
        shipment.setNodes(java.util.List.of(node));
        return shipment;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallLogisticsShipment appendNode(Long shipmentId, MallLogisticsNodeRequest request, String operatorId)
    {
        if (shipmentId == null || shipmentId <= 0 || request == null) throw new ServiceException("物流参数无效");
        MallLogisticsShipment shipment = logisticsMapper.selectByIdForUpdate(shipmentId);
        if (shipment == null) throw new ServiceException("物流单不存在");
        MallLogisticsNodeStatus status = MallLogisticsNodeStatus.parse(request.getNodeStatus());
        boolean usesServerTime = StringUtils.isBlank(request.getEventTime());
        LocalDateTime eventTime = parseEventTime(request.getEventTime());
        MallLogisticsNode latest = logisticsMapper.selectLatestNode(shipmentId);
        boolean delivered = "DELIVERED".equals(shipment.getStatus())
                || (latest != null && MallLogisticsNodeStatus.DELIVERED.name().equals(latest.getNodeStatus()));
        if (delivered && status != MallLogisticsNodeStatus.CORRECTION)
            throw new ServiceException("物流已签收，仅允许追加更正说明");
        if (latest != null && eventTime.isBefore(latest.getEventTime()))
        {
            if (!usesServerTime) throw new ServiceException("物流节点时间不能早于上一节点");
            eventTime = latest.getEventTime();
        }
        if (status == MallLogisticsNodeStatus.SHIPPED && latest != null) throw new ServiceException("已发货节点只能由发货操作创建");
        MallLogisticsNode node = new MallLogisticsNode();
        node.setShipmentId(shipmentId); node.setTrackingNo(shipment.getTrackingNo()); node.setNodeStatus(status.name());
        node.setTitle(request.getTitle().trim()); node.setDescription(request.getDescription().trim());
        node.setLocation(StringUtils.isBlank(request.getLocation()) ? null : request.getLocation().trim()); node.setEventTime(eventTime);
        if (logisticsMapper.insertNode(node) != 1) throw new ServiceException("物流轨迹保存失败");
        if (status == MallLogisticsNodeStatus.DELIVERED)
        {
            if (logisticsMapper.updateShipmentDelivered(shipmentId, eventTime) != 1)
                throw new ServiceException("物流单状态已变化，请刷新后重试");
            shipment.setStatus("DELIVERED");
            shipment.setDeliveredTime(eventTime);
        }
        MallOrderOperationLog log = new MallOrderOperationLog();
        log.setOrderId(shipment.getOrderId()); log.setOrderNo(shipment.getOrderNo());
        log.setFromStatus(latest == null ? "SHIPPED" : latest.getNodeStatus());
        log.setToStatus(status.name()); log.setOperatorType("ADMIN");
        log.setOperatorId(normalizeOperator(operatorId));
        log.setRemark("后台追加物流节点：" + status.getLabel()); log.setRequestId(shipment.getTrackingNo());
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
        publishLogisticsMessage(shipment, "物流状态更新", "订单 " + shipment.getOrderNo() + "：" + status.getLabel());
        return withNodes(shipment);
    }

    private void publishLogisticsMessage(MallLogisticsShipment shipment, String title, String summary)
    {
        if (memberMessagePort == null || shipment.getMemberId() == null) return;
        memberMessagePort.publish(shipment.getMemberId(), "LOGISTICS", title, summary, summary,
                "LOGISTICS", shipment.getOrderId(), shipment.getOrderNo(),
                "/orders/" + shipment.getOrderId() + "/logistics");
    }

    @Transactional(rollbackFor = Exception.class)
    public MallLogisticsShipment confirmReceipt(Long memberId, Long orderId)
    {
        if (memberId == null || memberId <= 0 || orderId == null || orderId <= 0) throw new ServiceException("收货参数无效");
        MallLogisticsShipment shipment = logisticsMapper.selectMemberShipment(orderId, memberId);
        if (shipment == null) throw new ServiceException("物流单不存在");
        if (!"DELIVERED".equals(shipment.getStatus()))
            throw new ServiceException("物流尚未签收，暂不能确认收货");
        int updated = orderMapper.markCompleted(orderId);
        if (updated != 1 && !"COMPLETED".equals(orderStatus(orderId, memberId)))
            throw new ServiceException("订单状态已变化，请刷新后重试");
        if (updated == 1)
            writeReceiptLog(orderId, shipment.getOrderNo(), memberId, "会员确认收货");
        return detailForMember(memberId, orderId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int autoConfirmReceipts()
    {
        int confirmed = 0;
        for (MallLogisticsShipment shipment : logisticsMapper.selectDeliveredBefore(LocalDateTime.now().minusDays(7)))
        {
            if (orderMapper.markCompleted(shipment.getOrderId()) == 1)
            {
                writeReceiptLog(shipment.getOrderId(), shipment.getOrderNo(), null, "签收满7天自动确认收货");
                confirmed++;
            }
        }
        return confirmed;
    }

    public MallLogisticsShipment detailForMember(Long memberId, Long orderId)
    {
        if (memberId == null || memberId <= 0 || orderId == null || orderId <= 0)
            throw new ServiceException("物流查询参数无效");
        MallLogisticsShipment shipment = logisticsMapper.selectMemberShipment(orderId, memberId);
        if (shipment == null) throw new ServiceException("物流单不存在");
        return withNodes(shipment);
    }

    public MallLogisticsShipment detailForAdmin(Long shipmentId)
    {
        if (shipmentId == null || shipmentId <= 0) throw new ServiceException("物流参数无效");
        MallLogisticsShipment shipment = logisticsMapper.selectById(shipmentId);
        if (shipment == null) throw new ServiceException("物流单不存在");
        return withNodes(shipment);
    }

    public List<MallFulfillmentOrder> listForAdmin(MallFulfillmentQuery query)
    {
        normalizeQuery(query);
        return logisticsMapper.selectFulfillmentOrders(query);
    }

    public MallFulfillmentSummary summaryForAdmin(MallFulfillmentQuery query)
    {
        normalizeQuery(query);
        return logisticsMapper.selectFulfillmentSummary(query);
    }

    public List<MallLogisticsCompanyOption> listCompanies()
    {
        return Arrays.stream(MallLogisticsCompany.values())
                .filter(company -> company != MallLogisticsCompany.MOCK)
                .map(company -> new MallLogisticsCompanyOption(company.getCode(), company.getName()))
                .collect(Collectors.toList());
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

    private LocalDateTime parseEventTime(String value)
    {
        if (StringUtils.isBlank(value)) return LocalDateTime.now();
        try { return LocalDateTime.parse(value); }
        catch (DateTimeParseException exception) { throw new ServiceException("物流节点时间格式无效"); }
    }

    private String orderStatus(Long orderId, Long memberId)
    {
        MallOrder order = orderMapper.selectByIdForUpdate(orderId, memberId);
        return order == null ? null : order.getStatus();
    }

    private void writeReceiptLog(Long orderId, String orderNo, Long memberId, String remark)
    {
        MallOrderOperationLog log = new MallOrderOperationLog(); log.setOrderId(orderId); log.setOrderNo(orderNo);
        log.setFromStatus("SHIPPED"); log.setToStatus("COMPLETED"); log.setOperatorType(memberId == null ? "SYSTEM" : "MEMBER");
        log.setOperatorId(memberId == null ? "receipt-auto-confirm" : String.valueOf(memberId)); log.setRemark(remark);
        if (orderMapper.insertOperationLog(log) != 1) throw new ServiceException("订单日志保存失败");
    }

    private String normalizeOperator(String operatorId)
    {
        return StringUtils.isBlank(operatorId) ? "system" : operatorId.length() > 64
                ? operatorId.substring(0, 64) : operatorId;
    }

    private void normalizeQuery(MallFulfillmentQuery query)
    {
        if (query == null) throw new ServiceException("履约查询参数不能为空");
        query.setOrderNo(trimToNull(query.getOrderNo()));
        query.setReceiverKeyword(trimToNull(query.getReceiverKeyword()));
        query.setCompanyCode(trimToUpper(query.getCompanyCode()));
        query.setWorkflowStatus(whitelist(trimToUpper(query.getWorkflowStatus()), WORKFLOW_STATUSES));
        query.setAttentionType(whitelist(trimToUpper(query.getAttentionType()), ATTENTION_TYPES));
        if (query.getCreateStart() != null && query.getCreateEnd() != null
                && query.getCreateStart().isAfter(query.getCreateEnd()))
            throw new ServiceException("下单开始时间不能晚于结束时间");
    }

    private String whitelist(String value, Set<String> allowed)
    {
        return value != null && allowed.contains(value) ? value : null;
    }

    private String trimToUpper(String value)
    {
        String normalized = trimToNull(value);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String trimToNull(String value)
    {
        if (StringUtils.isBlank(value)) return null;
        return value.trim();
    }

    private String safe(String value) { return value == null ? "" : value; }
}
