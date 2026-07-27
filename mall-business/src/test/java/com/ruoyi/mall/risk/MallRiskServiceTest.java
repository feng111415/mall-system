package com.ruoyi.mall.risk;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.order.domain.MallOrder;
import com.ruoyi.mall.order.mapper.MallOrderMapper;
import com.ruoyi.mall.risk.mapper.MallRiskMapper;
import com.ruoyi.mall.risk.service.MallRiskService;

class MallRiskServiceTest
{
    @Mock private MallRiskMapper mapper;
    @Mock private MallOrderMapper orderMapper;
    private MallRiskService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallRiskService(mapper, orderMapper);
    }

    @Test
    void fourthPendingOrderIsBlocked()
    {
        when(mapper.countPendingOrders(7L)).thenReturn(3);

        assertThrows(ServiceException.class, () -> service.checkOrder(7L, new BigDecimal("99.00")));
    }

    @Test
    void passedOrderCreatesAuditableRiskRecord()
    {
        MallOrder order = new MallOrder();
        order.setOrderId(1L); order.setOrderNo("M-1"); order.setMemberId(7L);
        order.setPayableAmount(new BigDecimal("99.00"));
        when(mapper.insertRecord(any())).thenReturn(1);
        when(orderMapper.markRiskPassed(1L)).thenReturn(1);

        service.recordPassed(order);

        verify(mapper).insertRecord(any());
    }
}
