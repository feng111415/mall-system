package com.ruoyi.mall.governance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.governance.domain.MallCompensationTask;
import com.ruoyi.mall.governance.mapper.MallCompensationMapper;
import com.ruoyi.mall.governance.service.MallCompensationService;

class MallCompensationServiceTest
{
    @Mock private MallCompensationMapper mapper;
    @Mock private InventoryPort inventoryPort;
    private MallCompensationService service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallCompensationService(mapper, inventoryPort);
    }

    @Test
    void taskCreationIsIdempotentByBusinessKey()
    {
        MallCompensationTask existing = task("INVENTORY_RELEASE", "ORDER-1", "SUCCESS");
        when(mapper.selectByBusinessKey("INVENTORY_RELEASE", "ORDER-1")).thenReturn(existing);

        assertEquals(existing, service.create("INVENTORY_RELEASE", "ORDER-1", "ORDER-1", null));
        verify(mapper, never()).insertIgnore(any());
    }

    @Test
    void dueInventoryTaskRunsOnceAndMarksSuccess()
    {
        MallCompensationTask task = task("INVENTORY_RELEASE", "ORDER-1", "PENDING");
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1);
        when(mapper.markSuccess(1L)).thenReturn(1);

        assertEquals(1, service.runDueTasks());
        verify(inventoryPort).release("ORDER-1");
        verify(mapper).markSuccess(1L);
    }

    @Test
    void unsupportedTaskMovesToManualAfterMaxRetry()
    {
        MallCompensationTask task = task("PAYMENT_RECONCILE", "PAY-1", "PENDING");
        task.setMaxRetries(1); task.setRetryCount(0);
        when(mapper.selectDue(50)).thenReturn(List.of(task));
        when(mapper.markProcessing(1L)).thenReturn(1);

        assertEquals(1, service.runDueTasks());
        verify(mapper).markFailure(eq(1L), eq("MANUAL"), any(), any());
    }

    private MallCompensationTask task(String type, String key, String status)
    {
        MallCompensationTask task = new MallCompensationTask();
        task.setTaskId(1L); task.setTaskType(type); task.setBusinessKey(key);
        task.setStatus(status); task.setRetryCount(0); task.setMaxRetries(5);
        return task;
    }
}
