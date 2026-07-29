-- Roll back V2.8.1 in an isolated verification database only.
delete from sys_job
where job_group='SYSTEM' and invoke_target='mallOrderTask.closeExpiredOrders';
