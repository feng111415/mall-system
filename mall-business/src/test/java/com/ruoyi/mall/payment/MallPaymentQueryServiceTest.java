package com.ruoyi.mall.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.payment.domain.MallPayment;
import com.ruoyi.mall.payment.mapper.MallPaymentMapper;
import com.ruoyi.mall.payment.service.MallPaymentQueryService;

class MallPaymentQueryServiceTest
{
    @Mock private MallPaymentMapper mapper;
    private MallPaymentQueryService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallPaymentQueryService(mapper);
    }

    @Test
    void listsPaymentsOnlyThroughMemberOwnedOrder()
    {
        when(mapper.selectMemberOrderPayments(1L, 7L)).thenReturn(List.of(new MallPayment()));

        assertEquals(1, service.listForOrder(7L, 1L).size());
        verify(mapper).selectMemberOrderPayments(1L, 7L);
        assertThrows(ServiceException.class, () -> service.listForOrder(null, 1L));
    }
}
