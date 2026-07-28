package com.ruoyi.mall.governance.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mall.application.port.InventoryPort;
import com.ruoyi.mall.governance.domain.MallCompensationTask;
import com.ruoyi.mall.governance.mapper.MallCompensationMapper;

@Service
public class MallCompensationService
{
    private static final int BATCH_SIZE = 50;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private final MallCompensationMapper mapper;
    private final InventoryPort inventoryPort;

    public MallCompensationService(MallCompensationMapper mapper, InventoryPort inventoryPort)
    {
        this.mapper = mapper;
        this.inventoryPort = inventoryPort;
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCompensationTask create(String taskType, String businessKey, String orderNo, String payload)
    {
        if (StringUtils.isBlank(taskType) || StringUtils.isBlank(businessKey))
            throw new ServiceException("补偿任务参数无效");
        MallCompensationTask existing = mapper.selectByBusinessKey(taskType, businessKey);
        if (existing != null) return existing;
        MallCompensationTask task = new MallCompensationTask();
        task.setTaskNo("COMP-" + UUID.randomUUID().toString().replace("-", ""));
        task.setTaskType(taskType.trim().toUpperCase()); task.setBusinessKey(businessKey.trim());
        task.setOrderNo(orderNo); task.setPayload(payload); task.setMaxRetries(DEFAULT_MAX_RETRIES);
        if (mapper.insertIgnore(task) != 1)
        {
            existing = mapper.selectByBusinessKey(task.getTaskType(), task.getBusinessKey());
            if (existing != null) return existing;
            throw new ServiceException("补偿任务保存失败");
        }
        task.setStatus("PENDING"); task.setRetryCount(0);
        return task;
    }

    public int runDueTasks()
    {
        mapper.recoverStaleProcessing(LocalDateTime.now().minusMinutes(10));
        int processed = 0;
        for (MallCompensationTask candidate : mapper.selectDue(BATCH_SIZE))
        {
            if (mapper.markProcessing(candidate.getTaskId()) != 1) continue;
            processed++;
            try
            {
                execute(candidate);
                if (mapper.markSuccess(candidate.getTaskId()) != 1)
                    throw new ServiceException("补偿任务成功状态保存失败");
            }
            catch (RuntimeException exception)
            {
                int retry = (candidate.getRetryCount() == null ? 0 : candidate.getRetryCount()) + 1;
                boolean manual = retry >= (candidate.getMaxRetries() == null ? DEFAULT_MAX_RETRIES : candidate.getMaxRetries());
                LocalDateTime next = LocalDateTime.now().plusMinutes(Math.min(60, 1L << Math.min(retry, 6)));
                mapper.markFailure(candidate.getTaskId(), manual ? "MANUAL" : "FAILED", next,
                        normalizeError(exception.getMessage()));
            }
        }
        return processed;
    }

    public List<MallCompensationTask> list(String status, String taskType, Integer limit, Integer offset)
    {
        int safeLimit = limit == null ? 20 : Math.min(Math.max(limit, 1), 100);
        int safeOffset = offset == null ? 0 : Math.max(offset, 0);
        return mapper.selectList(status, taskType, safeLimit, safeOffset);
    }

    @Transactional(rollbackFor = Exception.class)
    public MallCompensationTask retry(Long taskId)
    {
        if (taskId == null || taskId <= 0) throw new ServiceException("补偿任务参数无效");
        MallCompensationTask task = mapper.selectById(taskId);
        if (task == null) throw new ServiceException("补偿任务不存在");
        if ("SUCCESS".equals(task.getStatus())) return task;
        if (mapper.resetForRetry(taskId) != 1) throw new ServiceException("当前任务不允许重试");
        task.setStatus("PENDING"); task.setRetryCount(0); task.setLastError(null);
        return task;
    }

    private void execute(MallCompensationTask task)
    {
        if (inventoryPort == null) throw new IllegalStateException("InventoryPort 未配置");
        switch (task.getTaskType())
        {
            case "INVENTORY_RELEASE" -> inventoryPort.release(requireBusinessKey(task));
            case "INVENTORY_CONFIRM" -> inventoryPort.confirm(requireBusinessKey(task), true);
            default -> throw new ServiceException("暂未注册补偿处理器：" + task.getTaskType());
        }
    }

    private String requireBusinessKey(MallCompensationTask task)
    {
        if (StringUtils.isBlank(task.getBusinessKey())) throw new ServiceException("补偿业务键为空");
        return task.getBusinessKey();
    }

    private String normalizeError(String error)
    {
        String value = StringUtils.isBlank(error) ? "补偿执行失败" : error;
        return value.length() > 500 ? value.substring(0, 500) : value;
    }
}
