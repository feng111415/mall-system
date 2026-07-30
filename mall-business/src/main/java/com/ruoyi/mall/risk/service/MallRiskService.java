package com.ruoyi.mall.risk.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.risk.domain.MallRiskRecord;
import com.ruoyi.mall.risk.mapper.MallRiskMapper;

@Service
public class MallRiskService
{
    private static final int MAX_PENDING_ORDERS = 3;
    private final MallRiskMapper mapper;
    private final MallOrderMapper orderMapper;

    public MallRiskService(MallRiskMapper mapper, MallOrderMapper orderMapper)
    {
        this.mapper = mapper;
        this.orderMapper = orderMapper;
    }

    /** 下单前的基础频控，幂等订单在调用方已先返回，不会误伤重复点击。 */
    @Transactional(rollbackFor = Exception.class)
    public void checkOrder(Long memberId, BigDecimal amount)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
        if (mapper.lockMemberForOrder(memberId) == null)
            throw new ServiceException("会员不存在或已停用");
        if (mapper.countPendingOrders(memberId) >= MAX_PENDING_ORDERS)
            throw new ServiceException("待付款订单过多，请先完成支付或取消已有订单");
        if (amount == null || amount.signum() < 0) throw new ServiceException("订单金额无效");
    }

    public void recordPassed(MallOrder order)
    {
        if (orderMapper.markRiskPassed(order.getOrderId()) != 1)
            throw new ServiceException("订单风控状态保存失败");
        order.setRiskStatus("PASSED");
        MallRiskRecord record = new MallRiskRecord();
        record.setOrderId(order.getOrderId()); record.setOrderNo(order.getOrderNo());
        record.setMemberId(order.getMemberId()); record.setRuleCode("BASELINE");
        record.setRiskScore(0); record.setRiskStatus("PASSED"); record.setDecision("PASS");
        record.setReason("基础风控校验通过"); record.setOrderAmount(order.getPayableAmount());
        record.setOperatorType("SYSTEM"); record.setOperatorId("risk-engine");
        if (mapper.insertRecord(record) != 1) throw new ServiceException("风控记录保存失败");
    }

    public List<MallRiskRecord> records(String decision, Integer limit, Integer offset)
    {
        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 100);
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        return mapper.selectRecords(decision, safeLimit, safeOffset);
    }
}
