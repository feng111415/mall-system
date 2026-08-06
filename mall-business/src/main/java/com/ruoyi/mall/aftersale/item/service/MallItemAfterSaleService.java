package com.ruoyi.mall.aftersale.item.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSaleItem;
import com.ruoyi.mall.aftersale.item.domain.dto.MallItemAfterSaleItemRequest;
import com.ruoyi.mall.aftersale.item.domain.dto.MallItemAfterSaleRequest;
import com.ruoyi.mall.aftersale.item.domain.dto.MallReturnTrackingRequest;
import com.ruoyi.mall.aftersale.item.mapper.MallItemAfterSaleMapper;
import com.ruoyi.mall.logistics.domain.MallLogisticsCompany;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.domain.MallOrderItem;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.application.port.MemberMessagePort;

@Service
public class MallItemAfterSaleService
{
    private static final long AFTER_SALE_DAYS = 7L;
    private final MallItemAfterSaleMapper mapper;
    private final MallOrderMapper orderMapper;
    private final MallItemAfterSaleApprovalStateService approvalStateService;
    private final RefundPort refundPort;
    private final MemberMessagePort memberMessagePort;

    public MallItemAfterSaleService(MallItemAfterSaleMapper mapper, MallOrderMapper orderMapper,
            MallItemAfterSaleApprovalStateService approvalStateService, RefundPort refundPort)
    { this(mapper, orderMapper, approvalStateService, refundPort, null); }

    @Autowired
    public MallItemAfterSaleService(MallItemAfterSaleMapper mapper, MallOrderMapper orderMapper,
            MallItemAfterSaleApprovalStateService approvalStateService, RefundPort refundPort,
            MemberMessagePort memberMessagePort)
    {
        this.mapper = mapper; this.orderMapper = orderMapper; this.approvalStateService = approvalStateService; this.refundPort = refundPort;
        this.memberMessagePort = memberMessagePort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallItemAfterSale apply(Long memberId, Long orderId, MallItemAfterSaleRequest request)
    {
        requireMember(memberId);
        if (orderId == null || request == null) throw new ServiceException("售后参数无效");
        MallOrder order = orderMapper.selectByIdForUpdate(orderId, memberId);
        if (order == null) throw new ServiceException("订单不存在");
        if (!("SHIPPED".equals(order.getStatus()) || "COMPLETED".equals(order.getStatus())) || !"PAID".equals(order.getPaymentStatus()))
            throw new ServiceException("当前订单不支持售后");
        LocalDateTime deadline = order.getPayTime() == null
                ? LocalDateTime.now().plusDays(AFTER_SALE_DAYS)
                : order.getPayTime().plusDays(AFTER_SALE_DAYS);
        if (!LocalDateTime.now().isBefore(deadline))
            throw new ServiceException("订单已超过售后期限");
        String type = normalizeType(request.getType());
        String reasonCode = normalizeReasonCode(request.getReasonCode());
        if ("QUALITY".equals(reasonCode) && StringUtils.isBlank(request.getEvidenceUrl()))
            throw new ServiceException("质量问题需要提供凭证");
        List<MallOrderItem> orderItems = orderMapper.selectMemberOrderItems(orderId, memberId);
        Map<Long, MallOrderItem> byId = new HashMap<>();
        for (MallOrderItem item : orderItems) byId.put(item.getOrderItemId(), item);
        if (request.getItems() == null || request.getItems().isEmpty()) throw new ServiceException("至少选择一个订单项");
        MallItemAfterSale afterSale = new MallItemAfterSale();
        afterSale.setAfterSaleNo(generateNo()); afterSale.setOrderId(orderId); afterSale.setOrderNo(order.getOrderNo());
        afterSale.setMemberId(memberId); afterSale.setType(type); afterSale.setReasonCode(reasonCode);
        afterSale.setReason(trim(request.getReason(), 255)); afterSale.setEvidenceUrl(trim(request.getEvidenceUrl(), 1000));
        afterSale.setStatus("PENDING_REVIEW"); afterSale.setDeadlineTime(deadline);
        BigDecimal total = BigDecimal.ZERO; boolean fullOrder = true;
        Map<Long, Integer> quantities = new HashMap<>();
        for (MallItemAfterSaleItemRequest itemRequest : request.getItems())
        {
            if (itemRequest == null || itemRequest.getOrderItemId() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0)
                throw new ServiceException("售后数量无效");
            if (quantities.put(itemRequest.getOrderItemId(), itemRequest.getQuantity()) != null) throw new ServiceException("同一订单项不能重复申请");
            MallOrderItem item = byId.get(itemRequest.getOrderItemId());
            if (item == null) throw new ServiceException("订单项不属于当前订单");
            Integer activeValue = mapper.selectActiveRequestedQuantity(item.getOrderItemId());
            int active = activeValue == null ? 0 : activeValue;
            if (itemRequest.getQuantity() > item.getQuantity() - active) throw new ServiceException("申请数量超过可售后数量");
            if (itemRequest.getQuantity() < item.getQuantity()) fullOrder = false;
            total = total.add(item.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }
        BigDecimal shipping = fullOrder && quantities.size() == orderItems.size() ? safe(order.getShippingFee()) : BigDecimal.ZERO;
        afterSale.setRefundAmount(total.setScale(2, RoundingMode.HALF_UP)); afterSale.setShippingRefundAmount(shipping);
        afterSale.setRiskSignal("QUALITY".equals(reasonCode) ? null : "MANUAL_REVIEW");
        if (mapper.insert(afterSale) != 1) throw new ServiceException("售后申请保存失败");
        for (MallOrderItem item : orderItems)
        {
            Integer quantity = quantities.get(item.getOrderItemId());
            if (quantity == null) continue;
            MallItemAfterSaleItem detail = new MallItemAfterSaleItem(); detail.setAfterSaleId(afterSale.getAfterSaleId());
            detail.setOrderItemId(item.getOrderItemId()); detail.setSkuId(item.getSkuId()); detail.setProductName(item.getProductName()); detail.setSkuName(item.getSkuName());
            detail.setOrderQuantity(item.getQuantity()); detail.setRequestedQuantity(quantity); detail.setUnitPrice(item.getUnitPrice());
            detail.setRefundAmount(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP));
            if (mapper.insertItem(detail) != 1) throw new ServiceException("售后明细保存失败");
        }
        publishMessage(afterSale, "售后申请已提交", "售后单 " + afterSale.getAfterSaleNo() + " 等待审核");
        return afterSale;
    }

    public List<MallItemAfterSale> list(Long memberId)
    {
        requireMember(memberId); List<MallItemAfterSale> result = mapper.selectMemberList(memberId); result.forEach(x -> x.setItems(mapper.selectItems(x.getAfterSaleId()))); return result;
    }

    public MallItemAfterSale detail(Long memberId, Long afterSaleId)
    {
        requireMember(memberId); MallItemAfterSale value = mapper.selectMemberById(afterSaleId, memberId); if (value == null) throw new ServiceException("售后单不存在"); value.setItems(mapper.selectItems(afterSaleId)); return value;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallItemAfterSale submitReturnTracking(Long memberId, Long afterSaleId, MallReturnTrackingRequest request)
    {
        requireMember(memberId); if (request == null || StringUtils.isBlank(request.getCompanyCode()) || StringUtils.isBlank(request.getTrackingNo())) throw new ServiceException("退货物流信息不完整");
        MallLogisticsCompany company = MallLogisticsCompany.parse(request.getCompanyCode());
        if (MallLogisticsCompany.MOCK == company) throw new ServiceException("不支持的退货物流公司");
        MallItemAfterSale value = mapper.selectMemberByIdForUpdate(afterSaleId, memberId); if (value == null) throw new ServiceException("售后单不存在");
        String trackingNo = request.getTrackingNo().trim();
        if ("RETURN_SHIPPED".equals(value.getStatus()) && company.getCode().equals(value.getReturnCompanyCode())
                && trackingNo.equals(value.getReturnTrackingNo())) return value;
        if (!"RETURN_REFUND".equals(value.getType()) || !"APPROVED".equals(value.getStatus())) throw new ServiceException("当前售后单不允许提交退货物流");
        if (mapper.markReturnTracking(afterSaleId, company.getCode(), trackingNo) != 1) throw new ServiceException("退货物流提交失败");
        value.setReturnCompanyCode(company.getCode()); value.setReturnTrackingNo(trackingNo); value.setStatus("RETURN_SHIPPED"); return value;
    }

    public MallItemAfterSale approve(Long afterSaleId, String operatorId)
    {
        MallItemAfterSaleApprovalStateService.Approval approval = approvalStateService.prepare(afterSaleId, operatorId);
        if (!approval.shouldCallProvider()) return approval.completed();
        RefundPort.RefundResult result = refundPort.refund(approval.afterSaleNo(), approval.orderNo(), approval.paymentNo(), approval.amount());
        if (result == null || (result.success() && StringUtils.isBlank(result.providerRefundNo()))) throw new ServiceException("退款渠道结果不完整，售后单保留处理中");
        if (!result.success()) return approvalStateService.completeFailure(approval, StringUtils.isBlank(result.message()) ? "退款渠道拒绝" : result.message());
        return approvalStateService.completeSuccess(approval, result.providerRefundNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public MallItemAfterSale approveReview(Long afterSaleId)
    {
        MallItemAfterSale value = mapper.selectByIdForUpdate(afterSaleId);
        if (value == null) throw new ServiceException("售后单不存在");
        if ("APPROVED".equals(value.getStatus()) || "RETURN_SHIPPED".equals(value.getStatus())) return value;
        if (!"PENDING_REVIEW".equals(value.getStatus())) throw new ServiceException("当前售后单不允许审核通过");
        if (mapper.markApproved(afterSaleId) != 1) throw new ServiceException("售后单状态已变化，请刷新后重试");
        value.setStatus("APPROVED");
        publishMessage(value, "售后审核通过", "售后单 " + value.getAfterSaleNo() + " 已审核通过");
        return value;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallItemAfterSale reject(Long afterSaleId, String operatorId, String reason)
    {
        MallItemAfterSale value = mapper.selectByIdForUpdate(afterSaleId); if (value == null) throw new ServiceException("售后单不存在");
        if ("REJECTED".equals(value.getStatus())) return value;
        if (!"PENDING_REVIEW".equals(value.getStatus())) throw new ServiceException("当前售后单不允许驳回");
        String message = StringUtils.isBlank(reason) ? "售后申请未通过" : trim(reason, 255);
        if (mapper.markRejected(afterSaleId, message) != 1) throw new ServiceException("售后单状态已变化，请刷新后重试");
        value.setStatus("REJECTED"); value.setFailureReason(message);
        publishMessage(value, "售后申请未通过", "售后单 " + value.getAfterSaleNo() + " 未通过审核");
        return value;
    }

    public List<MallItemAfterSale> adminList(String status) { return mapper.selectAdminList(status); }
    public MallItemAfterSale adminDetail(Long afterSaleId) { MallItemAfterSale value=mapper.selectById(afterSaleId); if(value==null) throw new ServiceException("售后单不存在"); value.setItems(mapper.selectItems(afterSaleId)); return value; }

    private String normalizeType(String value) { if (value == null) throw new ServiceException("售后类型不能为空"); String v=value.trim().toUpperCase(); if (!"ONLY_REFUND".equals(v) && !"RETURN_REFUND".equals(v)) throw new ServiceException("售后类型无效"); return v; }
    private String normalizeReasonCode(String value) { if (StringUtils.isBlank(value)) throw new ServiceException("售后原因不能为空"); return value.trim().toUpperCase(); }
    private String generateNo() { return "AS" + System.currentTimeMillis() + String.format("%04d", ThreadLocalRandom.current().nextInt(10000)); }
    private String trim(String value, int max) { if (value == null) return null; String v=value.trim(); return v.length()>max?v.substring(0,max):v; }
    private BigDecimal safe(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private void requireMember(Long memberId) { if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效"); }

    private void publishMessage(MallItemAfterSale value, String title, String summary)
    {
        if (memberMessagePort != null)
            memberMessagePort.publish(value.getMemberId(), "AFTER_SALE", title, summary, summary,
                    "AFTER_SALE", value.getAfterSaleId(), value.getAfterSaleNo(),
                    "/orders/" + value.getOrderId());
    }
}
