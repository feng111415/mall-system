-- Mall governance: durable compensation tasks with idempotent business keys.
create table if not exists mall_compensation_task (
 task_id bigint not null auto_increment, task_no varchar(64) not null,
 task_type varchar(32) not null, business_key varchar(128) not null,
 order_no varchar(64), payload varchar(2000), status varchar(16) not null default 'PENDING',
 retry_count int not null default 0, max_retries int not null default 5,
 next_retry_time datetime not null, last_error varchar(500),
 create_time datetime not null, update_time datetime not null, completed_time datetime,
 primary key(task_id), unique key uk_mall_compensation_task_no(task_no),
 unique key uk_mall_compensation_business(task_type,business_key),
 key idx_mall_compensation_due(status,next_retry_time), key idx_mall_compensation_order(order_no),
 constraint ck_mall_compensation_status check(status in ('PENDING','PROCESSING','SUCCESS','FAILED','MANUAL')),
 constraint ck_mall_compensation_retry check(retry_count>=0 and max_retries>0)
) engine=InnoDB default charset=utf8mb4 comment='商城补偿任务';
