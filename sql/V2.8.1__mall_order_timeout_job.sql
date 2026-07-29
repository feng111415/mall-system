-- Register the order timeout worker once. It closes UNPAID orders at 30 minutes
-- and PAYING orders at 35 minutes through MallOrderLifecycleService.
insert into sys_job(job_name, job_group, invoke_target, cron_expression, misfire_policy,
    concurrent, status, create_by, create_time, remark)
select 'Mall order payment timeout close', 'SYSTEM', 'mallOrderTask.closeExpiredOrders', '0 0/1 * * * ?', '3',
    '1', '0', 'mall-system', now(), 'Close expired orders and release locked stock'
where not exists (
    select 1 from sys_job
    where job_group='SYSTEM' and invoke_target='mallOrderTask.closeExpiredOrders'
);
