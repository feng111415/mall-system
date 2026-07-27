-- Mall phase five: separate risk status and auditable risk decisions.
alter table mall_order add column risk_status varchar(16) not null default 'PENDING_CHECK' after payment_status;
alter table mall_order add key idx_mall_order_member_risk(member_id,risk_status,create_time);
alter table mall_order add constraint ck_mall_order_risk_status check(risk_status in ('PENDING_CHECK','PASSED','REVIEW','REJECTED'));

create table if not exists mall_risk_record (
 risk_id bigint not null auto_increment, order_id bigint, order_no varchar(64), member_id bigint not null,
 rule_code varchar(64) not null, risk_score int not null default 0, risk_status varchar(16) not null,
 decision varchar(16) not null, reason varchar(500) not null, order_amount decimal(12,2),
 operator_type varchar(16) not null, operator_id varchar(64), create_time datetime not null,
 primary key(risk_id), key idx_mall_risk_member(member_id,create_time), key idx_mall_risk_order(order_id),
 key idx_mall_risk_decision(decision,create_time),
 constraint ck_mall_risk_status check(risk_status in ('PENDING_CHECK','PASSED','REVIEW','REJECTED')),
 constraint ck_mall_risk_decision check(decision in ('PASS','REVIEW','REJECT'))
) engine=InnoDB default charset=utf8mb4 comment='商城风控记录';
