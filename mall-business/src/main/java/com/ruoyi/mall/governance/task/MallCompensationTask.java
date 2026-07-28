package com.ruoyi.mall.governance.task;

import org.springframework.stereotype.Component;
import com.ruoyi.mall.governance.service.MallCompensationService;

/** Quartz target: mallCompensationTask.runDueTasks() */
@Component("mallCompensationTask")
public class MallCompensationTask
{
    private final MallCompensationService service;

    public MallCompensationTask(MallCompensationService service) { this.service = service; }

    public int runDueTasks() { return service.runDueTasks(); }
}
