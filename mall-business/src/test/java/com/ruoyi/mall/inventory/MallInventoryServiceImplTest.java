package com.ruoyi.mall.inventory;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.inventory.domain.MallStock;
import com.ruoyi.mall.inventory.domain.MallStockReservation;
import com.ruoyi.mall.inventory.domain.dto.MallStockAdjustRequest;
import com.ruoyi.mall.inventory.mapper.MallInventoryMapper;
import com.ruoyi.mall.inventory.service.impl.MallInventoryServiceImpl;

class MallInventoryServiceImplTest
{
    @Mock private MallInventoryMapper mapper;
    private MallInventoryServiceImpl service;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        service = new MallInventoryServiceImpl(mapper);
    }

    @Test
    void rejectsInvalidReserveWithoutWriting()
    {
        assertThrows(ServiceException.class, () -> service.reserve("ORDER-1", 10L, 0));
        verify(mapper, never()).insertStockIfAbsent(10L);
    }

    @Test
    void repeatedReserveIsIdempotent()
    {
        when(mapper.selectBySkuIdForUpdate(10L)).thenReturn(stock(4, 0, 0));
        MallStockReservation existing = reservation("ORDER-1", 10L, 2, "LOCKED");
        when(mapper.selectReservationForUpdate("ORDER-1", 10L)).thenReturn(existing);

        service.reserve("ORDER-1", 10L, 2);

        verify(mapper, never()).reserveStock(10L, 2);
        verify(mapper, never()).insertLog(any());
    }

    @Test
    void rejectsReserveWhenAvailableStockIsInsufficient()
    {
        when(mapper.selectBySkuIdForUpdate(10L)).thenReturn(stock(1, 0, 0));
        when(mapper.selectReservationForUpdate("ORDER-1", 10L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.reserve("ORDER-1", 10L, 2));
        verify(mapper, never()).insertReservationIgnore(any());
    }

    @Test
    void unpaidOrderCannotConfirm()
    {
        assertThrows(ServiceException.class, () -> service.confirm("ORDER-1", false));
        verify(mapper, never()).selectReservationsForUpdate("ORDER-1");
    }

    @Test
    void releaseOnlyChangesLockedReservation()
    {
        when(mapper.selectReservationsForUpdate("ORDER-1"))
                .thenReturn(List.of(reservation("ORDER-1", 10L, 2, "RELEASED")));

        service.release("ORDER-1");

        verify(mapper, never()).releaseStock(10L, 2);
        verify(mapper, never()).insertLog(any());
    }

    @Test
    void adjustmentRequiresReason()
    {
        MallStockAdjustRequest request = new MallStockAdjustRequest();
        request.setDelta(2);
        assertThrows(ServiceException.class, () -> service.adjust(10L, request, "admin"));
        verify(mapper, never()).selectBySkuIdForUpdate(10L);
    }

    @Test
    void lowStockFilterIsPassedToSqlBeforePaging()
    {
        when(mapper.selectStockList(any())).thenReturn(List.of());

        service.selectStocks(new MallStock(), true);

        verify(mapper).selectStockList(org.mockito.ArgumentMatchers.argThat(value -> "LOW_STOCK".equals(value.getStockStatus())));
    }

    @Test
    void warningThresholdCanBeUpdatedWithoutChangingQuantity()
    {
        MallStockAdjustRequest request = new MallStockAdjustRequest();
        request.setWarningThreshold(3); request.setReason("调整预警阈值");
        when(mapper.selectBySkuIdForUpdate(10L)).thenReturn(stock(8, 0, 0));
        when(mapper.adjustStock(10L, 0, 3)).thenReturn(1);

        service.adjust(10L, request, "admin");

        verify(mapper).adjustStock(10L, 0, 3);
        verify(mapper).insertLog(any());
    }

    private MallStock stock(int available, int locked, int sold)
    {
        MallStock stock = new MallStock();
        stock.setSkuId(10L); stock.setAvailableQuantity(available); stock.setLockedQuantity(locked); stock.setSoldQuantity(sold);
        return stock;
    }

    private MallStockReservation reservation(String orderNo, Long skuId, int quantity, String status)
    {
        MallStockReservation value = new MallStockReservation();
        value.setReservationId(1L); value.setOrderNo(orderNo); value.setSkuId(skuId); value.setQuantity(quantity); value.setStatus(status);
        return value;
    }
}
