-- Payment/refund reconciliation differences. Reconciliation records facts only;
-- it never mutates payment or refund state automatically.
create table if not exists mall_reconciliation_diff (
 diff_id bigint not null auto_increment,
 diff_no varchar(64) not null,
 diff_type varchar(16) not null,
 business_no varchar(128) not null,
 order_no varchar(64),
 local_status varchar(32),
 external_status varchar(32),
 local_amount decimal(18,2),
 external_amount decimal(18,2),
 diff_code varchar(32) not null,
 diff_message varchar(500),
 status varchar(16) not null default 'OPEN',
 handle_remark varchar(500),
 create_time datetime not null,
 update_time datetime not null,
 resolved_time datetime,
 primary key(diff_id),
 unique key uk_mall_reconciliation_diff_no(diff_no),
 unique key uk_mall_reconciliation_business(diff_type,business_no,diff_code),
 key idx_mall_reconciliation_status(status,create_time),
 key idx_mall_reconciliation_order(order_no),
 constraint ck_mall_reconciliation_type check(diff_type in ('PAYMENT','REFUND')),
 constraint ck_mall_reconciliation_status check(status in ('OPEN','PROCESSING','RESOLVED','IGNORED','MANUAL'))
) engine=InnoDB default charset=utf8mb4 comment='商城支付退款对账差异';
