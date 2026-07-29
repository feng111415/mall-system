package com.ruoyi.mall.payment.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;

@Service
public class MallPaymentQueryService
{
    private final MallPaymentMapper mapper;

    public MallPaymentQueryService(MallPaymentMapper mapper) { this.mapper = mapper; }

    public List<MallPayment> listForOrder(Long memberId, Long orderId)
    {
        if (memberId == null || memberId <= 0 || orderId == null || orderId <= 0)
            throw new ServiceException("支付查询参数无效");
        List<MallPayment> payments = mapper.selectMemberOrderPayments(orderId, memberId);
        payments.forEach(payment -> payment.setIdempotencyKey(null));
        return payments;
    }
}
