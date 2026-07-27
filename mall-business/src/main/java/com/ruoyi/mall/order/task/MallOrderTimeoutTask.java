package com.ruoyi.mall.order.task;

import org.springframework.stereotype.Component;
import com.ruoyi.mall.order.service.MallOrderLifecycleService;

/** Quartz target: mallOrderTask.closeExpiredOrders() */
@Component("mallOrderTask")
public class MallOrderTimeoutTask
{
    private final MallOrderLifecycleService lifecycleService;

    public MallOrderTimeoutTask(MallOrderLifecycleService lifecycleService)
    {
        this.lifecycleService = lifecycleService;
    }

    public void closeExpiredOrders()
    {
        lifecycleService.closeExpiredOrders();
    }
}
