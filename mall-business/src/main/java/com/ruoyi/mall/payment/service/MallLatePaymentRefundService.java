package com.ruoyi.mall.payment.service;

import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.RefundPort;
import com.ruoyi.mall.payment.domain.MallPayment;

@Service
public class MallLatePaymentRefundService
{
    private final RefundPort refundPort;
    private final MallLatePaymentRefundStateService stateService;

    public MallLatePaymentRefundService(RefundPort refundPort, MallLatePaymentRefundStateService stateService)
    {
        this.refundPort = refundPort;
        this.stateService = stateService;
    }

    public MallPayment refund(String paymentNo)
    {
        if (StringUtils.isBlank(paymentNo)) throw new ServiceException("支付单参数无效");
        MallLatePaymentRefundStateService.LateRefundPreparation preparation = stateService.prepare(paymentNo);
        if (!preparation.shouldCallProvider()) return preparation.completedPayment();
        RefundPort.RefundResult result = refundPort.refund(preparation.refundNo(), preparation.orderNo(),
                preparation.paymentNo(), preparation.amount());
        if (result == null || !result.success() || StringUtils.isBlank(result.providerRefundNo()))
            throw new ServiceException(result == null ? "退款渠道未返回结果"
                    : StringUtils.isBlank(result.message()) ? "异常支付退款失败" : result.message());
        return stateService.completeSuccess(preparation, result.providerRefundNo());
    }
}
