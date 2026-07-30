package com.ruoyi.mall.aftersale.item.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.aftersale.item.domain.MallItemAfterSale;
import com.ruoyi.mall.aftersale.item.mapper.MallItemAfterSaleMapper;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

@Service
public class MallItemAfterSaleApprovalStateService
{
    private final MallItemAfterSaleMapper mapper;
    private final MallPaymentMapper paymentMapper;

    public MallItemAfterSaleApprovalStateService(MallItemAfterSaleMapper mapper, MallPaymentMapper paymentMapper)
    { this.mapper = mapper; this.paymentMapper = paymentMapper; }

    @Transactional(rollbackFor = Exception.class)
    public Approval prepare(Long afterSaleId, String operatorId)
    {
        MallItemAfterSale item = require(afterSaleId);
        if ("SUCCESS".equals(item.getStatus())) return Approval.completed(item);
        if (!("APPROVED".equals(item.getStatus()) || "RETURN_SHIPPED".equals(item.getStatus()) || "REFUNDING".equals(item.getStatus())))
            throw new ServiceException("当前售后单不允许退款");
        if ("RETURN_REFUND".equals(item.getType()) && !"RETURN_SHIPPED".equals(item.getStatus()) && !"REFUNDING".equals(item.getStatus()))
            throw new ServiceException("退货退款必须先提交退货物流");
        if (!"REFUNDING".equals(item.getStatus()) && mapper.markRefunding(afterSaleId) != 1)
            throw new ServiceException("售后单状态已变化，请刷新后重试");
        MallPayment payment = paymentMapper.selectSuccessByOrderIdForUpdate(item.getOrderId());
        if (payment == null || payment.getPaymentNo() == null) throw new ServiceException("原支付单不存在，不能发起退款");
        BigDecimal amount = safe(item.getRefundAmount()).add(safe(item.getShippingRefundAmount()));
        return new Approval(afterSaleId, item.getAfterSaleNo(), item.getOrderNo(), payment.getPaymentNo(), amount, operatorId, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public MallItemAfterSale completeSuccess(Approval approval, String providerRefundNo)
    {
        MallItemAfterSale item = require(approval.afterSaleId());
        if ("SUCCESS".equals(item.getStatus())) return item;
        if (!"REFUNDING".equals(item.getStatus()) || providerRefundNo == null || providerRefundNo.isBlank())
            throw new ServiceException("售后退款结果无效");
        if (mapper.markSuccess(item.getAfterSaleId(), providerRefundNo) != 1) throw new ServiceException("售后退款结果保存失败");
        item.setStatus("SUCCESS"); item.setProviderRefundNo(providerRefundNo); return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallItemAfterSale completeFailure(Approval approval, String reason)
    {
        MallItemAfterSale item = require(approval.afterSaleId());
        if ("SUCCESS".equals(item.getStatus())) return item;
        if (!"REFUNDING".equals(item.getStatus())) throw new ServiceException("售后退款状态无效");
        String message = reason == null || reason.isBlank() ? "退款渠道失败" : reason;
        if (mapper.markFailed(item.getAfterSaleId(), message) != 1) throw new ServiceException("售后退款失败状态保存失败");
        item.setStatus("FAILED"); item.setFailureReason(message); return item;
    }

    private MallItemAfterSale require(Long id) { MallItemAfterSale value = mapper.selectByIdForUpdate(id); if (value == null) throw new ServiceException("售后单不存在"); return value; }
    private BigDecimal safe(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    public record Approval(Long afterSaleId, String afterSaleNo, String orderNo, String paymentNo, BigDecimal amount, String operatorId, MallItemAfterSale completed) {
        static Approval completed(MallItemAfterSale value) { return new Approval(value.getAfterSaleId(), value.getAfterSaleNo(), value.getOrderNo(), null, null, null, value); }
        public boolean shouldCallProvider() { return completed == null; }
    }
}
