package com.ruoyi.mall.reconciliation.task;

import org.springframework.stereotype.Component;
import com.ruoyi.mall.reconciliation.service.MallReconciliationService;

/** Quartz target: mallReconciliationTask.generateAlerts() */
@Component("mallReconciliationTask")
public class MallReconciliationTask {
    private final MallReconciliationService service;
    public MallReconciliationTask(MallReconciliationService service){this.service=service;}
    public int generateAlerts(){return service.generateAlerts();}
}
