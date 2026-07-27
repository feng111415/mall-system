package com.ruoyi.mall.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.audit.domain.MallOrderOperationLogQuery;
import com.ruoyi.mall.audit.mapper.MallAuditMapper;
import com.ruoyi.mall.audit.service.MallAuditService;
import com.ruoyi.mall.order.domain.MallOrderOperationLog;

class MallAuditServiceTest
{
    @Mock private MallAuditMapper mapper;
    private MallAuditService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallAuditService(mapper);
    }

    @Test
    void returnsFilteredOrderOperationLogs()
    {
        MallOrderOperationLogQuery query = new MallOrderOperationLogQuery();
        query.setOrderNo("M-1"); query.setOperatorType("ADMIN");
        when(mapper.selectOrderOperationLogs(query)).thenReturn(List.of(new MallOrderOperationLog()));

        assertEquals(1, service.orderOperationLogs(query).size());
        verify(mapper).selectOrderOperationLogs(query);
    }
}
