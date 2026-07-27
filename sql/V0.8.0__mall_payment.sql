-- Mall phase three: payment attempts and simulated payment results.
create table if not exists mall_payment (
 payment_id bigint not null auto_increment, payment_no varchar(64) not null,
 order_id bigint not null, order_no varchar(64) not null, member_id bigint not null,
 payment_method varchar(32) not null default 'MOCK', amount decimal(12,2) not null,
 status varchar(16) not null default 'CREATING', idempotency_key varchar(80) not null,
 provider_payment_no varchar(128), payment_url varchar(500), failure_reason varchar(255),
 paid_time datetime, create_time datetime not null, update_time datetime not null,
 primary key(payment_id), unique key uk_mall_payment_no(payment_no),
 unique key uk_mall_payment_order_idem(order_id,idempotency_key),
 unique key uk_mall_payment_provider_no(provider_payment_no),
 key idx_mall_payment_member_status(member_id,status,create_time),
 constraint ck_mall_payment_amount check(amount>=0),
 constraint ck_mall_payment_status check(status in ('CREATING','PAYING','SUCCESS','FAILED','CLOSED'))
) engine=InnoDB default charset=utf8mb4 comment='商城支付单';
