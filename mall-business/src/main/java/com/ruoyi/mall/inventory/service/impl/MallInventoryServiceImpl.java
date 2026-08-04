package com.ruoyi.mall.inventory.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.inventory.domain.MallStock;
import com.ruoyi.mall.inventory.domain.MallStockLog;
import com.ruoyi.mall.inventory.domain.MallStockReservation;
import com.ruoyi.mall.inventory.domain.dto.MallStockAdjustRequest;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.inventory.mapper.MallInventoryMapper;
import com.ruoyi.mall.inventory.service.IMallInventoryService;

@Service
public class MallInventoryServiceImpl implements IMallInventoryService, InventoryPort
{
    private final MallInventoryMapper mapper;

    public MallInventoryServiceImpl(MallInventoryMapper mapper) { this.mapper = mapper; }

    @Override
    public List<MallStock> selectStocks(MallStock query, boolean lowStockOnly)
    {
        mapper.insertAllStocksIfAbsent();
        if (query == null) query = new MallStock();
        if (lowStockOnly && StringUtils.isBlank(query.getStockStatus())) query.setStockStatus("LOW_STOCK");
        return mapper.selectStockList(query);
    }

    @Override
    public MallStock selectStock(Long skuId)
    {
        requireSkuId(skuId);
        mapper.insertStockIfAbsent(skuId);
        return mapper.selectBySkuId(skuId);
    }

    @Override
    public List<MallStockLog> selectLogs(Long skuId, String operationType)
    {
        requireSkuId(skuId);
        return mapper.selectLogList(skuId, operationType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjust(Long skuId, MallStockAdjustRequest request, String operator)
    {
        requireSkuId(skuId);
        if (request == null) throw new ServiceException("库存调整参数不能为空");
        int delta = request.getDelta() == null ? 0 : request.getDelta();
        if (delta == 0 && request.getWarningThreshold() == null)
            throw new ServiceException("库存调整数量和预警阈值不能同时为空");
        if (StringUtils.isBlank(request.getReason())) throw new ServiceException("库存调整必须填写原因");
        if (request.getWarningThreshold() != null && request.getWarningThreshold() < 0)
            throw new ServiceException("低库存阈值不能为负数");
        MallStock before = lockStock(skuId);
        int afterAvailable = before.getAvailableQuantity() + delta;
        if (afterAvailable < 0) throw new ServiceException("可用库存不足，不能减少库存");
        if (mapper.adjustStock(skuId, delta, request.getWarningThreshold()) != 1)
            throw new ServiceException("库存调整失败，请重试");
        mapper.syncLegacySkuStock(skuId);
        insertLog(skuId, "ADJUST", "ADMIN", null, delta, 0, 0,
                afterAvailable, before.getLockedQuantity(), before.getSoldQuantity(), request.getReason(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reserve(String orderNo, Long skuId, int quantity)
    {
        requireOrderNo(orderNo);
        requireSkuId(skuId);
        if (quantity <= 0) throw new ServiceException("预占数量必须大于 0");
        mapper.insertStockIfAbsent(skuId);
        MallStock stock = lockStock(skuId);
        MallStockReservation existing = mapper.selectReservationForUpdate(orderNo, skuId);
        if (existing != null)
        {
            if (existing.getQuantity() == quantity
                    && ("LOCKED".equals(existing.getStatus()) || "CONFIRMED".equals(existing.getStatus()))) return;
            throw new ServiceException("同一订单的库存预占不能重复或修改");
        }
        if (stock.getAvailableQuantity() < quantity) throw new ServiceException("库存不足");
        MallStockReservation reservation = new MallStockReservation();
        reservation.setOrderNo(orderNo);
        reservation.setSkuId(skuId);
        reservation.setQuantity(quantity);
        reservation.setStatus("LOCKED");
        if (mapper.insertReservationIgnore(reservation) == 0)
        {
            MallStockReservation concurrent = mapper.selectReservationForUpdate(orderNo, skuId);
            if (concurrent != null && concurrent.getQuantity() == quantity
                    && ("LOCKED".equals(concurrent.getStatus()) || "CONFIRMED".equals(concurrent.getStatus()))) return;
            throw new ServiceException("库存预占请求重复，请重试");
        }
        if (mapper.reserveStock(skuId, quantity) != 1) throw new ServiceException("库存不足");
        mapper.syncLegacySkuStock(skuId);
        insertLog(skuId, "RESERVE", "ORDER", orderNo, -quantity, quantity, 0,
                stock.getAvailableQuantity() - quantity, stock.getLockedQuantity() + quantity,
                stock.getSoldQuantity(), "订单锁定库存", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void release(String orderNo)
    {
        requireOrderNo(orderNo);
        for (MallStockReservation reservation : mapper.selectReservationsForUpdate(orderNo))
        {
            if (!"LOCKED".equals(reservation.getStatus())) continue;
            MallStock before = lockStock(reservation.getSkuId());
            MallStockReservation current = mapper.selectReservationForUpdate(orderNo, reservation.getSkuId());
            if (current == null || !"LOCKED".equals(current.getStatus())) continue;
            reservation = current;
            if (mapper.releaseStock(reservation.getSkuId(), reservation.getQuantity()) != 1)
                throw new ServiceException("锁定库存释放失败");
            mapper.updateReservationStatus(reservation.getReservationId(), "RELEASED");
            mapper.syncLegacySkuStock(reservation.getSkuId());
            insertLog(reservation.getSkuId(), "RELEASE", "ORDER", orderNo, reservation.getQuantity(),
                    -reservation.getQuantity(), 0, before.getAvailableQuantity() + reservation.getQuantity(),
                    before.getLockedQuantity() - reservation.getQuantity(), before.getSoldQuantity(), "订单取消释放库存", null);
        }
    }

    @Override
    public void confirm(String orderNo) { confirm(orderNo, true); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirm(String orderNo, boolean paymentConfirmed)
    {
        requireOrderNo(orderNo);
        if (!paymentConfirmed) throw new ServiceException("未支付订单不能确认扣减库存");
        for (MallStockReservation reservation : mapper.selectReservationsForUpdate(orderNo))
        {
            if (!"LOCKED".equals(reservation.getStatus())) continue;
            MallStock before = lockStock(reservation.getSkuId());
            MallStockReservation current = mapper.selectReservationForUpdate(orderNo, reservation.getSkuId());
            if (current == null || !"LOCKED".equals(current.getStatus())) continue;
            reservation = current;
            if (mapper.confirmStock(reservation.getSkuId(), reservation.getQuantity()) != 1)
                throw new ServiceException("库存确认扣减失败");
            mapper.updateReservationStatus(reservation.getReservationId(), "CONFIRMED");
            mapper.syncLegacySkuStock(reservation.getSkuId());
            insertLog(reservation.getSkuId(), "CONFIRM", "ORDER", orderNo, 0,
                    -reservation.getQuantity(), reservation.getQuantity(), before.getAvailableQuantity(),
                    before.getLockedQuantity() - reservation.getQuantity(), before.getSoldQuantity() + reservation.getQuantity(),
                    "支付成功确认销售", null);
        }
    }

    private MallStock lockStock(Long skuId)
    {
        MallStock stock = mapper.selectBySkuIdForUpdate(skuId);
        if (stock == null) throw new ServiceException("SKU 不存在或未初始化库存");
        if (stock.getAvailableQuantity() == null || stock.getLockedQuantity() == null || stock.getSoldQuantity() == null)
            throw new ServiceException("库存数据不完整");
        return stock;
    }

    private void insertLog(Long skuId, String operationType, String sourceType, String sourceNo,
        int availableChange, int lockedChange, int soldChange, int availableAfter, int lockedAfter,
        int soldAfter, String reason, String operator)
    {
        MallStockLog log = new MallStockLog();
        log.setSkuId(skuId); log.setOperationType(operationType); log.setSourceType(sourceType);
        log.setSourceNo(sourceNo); log.setAvailableChange(availableChange); log.setLockedChange(lockedChange);
        log.setSoldChange(soldChange); log.setAvailableAfter(availableAfter); log.setLockedAfter(lockedAfter);
        log.setSoldAfter(soldAfter); log.setReason(reason); log.setOperator(operator);
        mapper.insertLog(log);
    }

    private static void requireSkuId(Long skuId) { if (skuId == null || skuId <= 0) throw new ServiceException("SKU 无效"); }
    private static void requireOrderNo(String orderNo) { if (StringUtils.isBlank(orderNo)) throw new ServiceException("订单号不能为空"); }
}
