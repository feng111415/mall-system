-- Durable alerts for reconciliation differences that remain unresolved.
create table if not exists mall_reconciliation_alert (
 alert_id bigint not null auto_increment,
 alert_no varchar(64) not null,
 diff_id bigint not null,
 alert_level varchar(16) not null default 'HIGH',
 status varchar(16) not null default 'OPEN',
 alert_message varchar(500) not null,
 acknowledged_by varchar(64),
 acknowledged_time datetime,
 create_time datetime not null,
 update_time datetime not null,
 primary key(alert_id),
 unique key uk_mall_reconciliation_alert_no(alert_no),
 unique key uk_mall_reconciliation_alert_diff(diff_id),
 key idx_mall_reconciliation_alert_status(status,create_time),
 constraint ck_mall_reconciliation_alert_level check(alert_level in ('MEDIUM','HIGH','CRITICAL')),
 constraint ck_mall_reconciliation_alert_status check(status in ('OPEN','ACKED'))
) engine=InnoDB default charset=utf8mb4 comment='商城对账差异告警';
