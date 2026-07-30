package com.ruoyi.mall.logistics.task;

import org.springframework.stereotype.Component;
import com.ruoyi.mall.logistics.service.MallLogisticsService;

/** Quartz target: mallReceiptTask.autoConfirmReceipts(). */
@Component("mallReceiptTask")
public class MallReceiptAutoConfirmTask
{
    private final MallLogisticsService logisticsService;

    public MallReceiptAutoConfirmTask(MallLogisticsService logisticsService)
    {
        this.logisticsService = logisticsService;
    }

    public void autoConfirmReceipts()
    {
        logisticsService.autoConfirmReceipts();
    }
}
