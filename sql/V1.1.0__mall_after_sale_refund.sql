-- Mall phase four: full-order after-sale refund workflow.
create table if not exists mall_refund (
 refund_id bigint not null auto_increment, refund_no varchar(64) not null,
 order_id bigint not null, order_no varchar(64) not null, member_id bigint not null,
 refund_amount decimal(12,2) not null, reason varchar(255) not null,
 status varchar(16) not null default 'APPLIED', original_order_status varchar(24) not null,
 provider_refund_no varchar(128), failure_reason varchar(255), refund_time datetime,
 create_time datetime not null, update_time datetime not null,
 primary key(refund_id), unique key uk_mall_refund_no(refund_no),
 unique key uk_mall_refund_order(order_id), unique key uk_mall_refund_provider(provider_refund_no),
 key idx_mall_refund_member(member_id,create_time),
 constraint ck_mall_refund_amount check(refund_amount>=0),
 constraint ck_mall_refund_status check(status in ('APPLIED','REFUNDING','SUCCESS','REJECTED','FAILED'))
) engine=InnoDB default charset=utf8mb4 comment='商城售后退款单';
