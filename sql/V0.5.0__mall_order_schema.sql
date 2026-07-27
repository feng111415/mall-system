-- Mall phase three: order schema and immutable product snapshots.
create table if not exists mall_order (
 order_id bigint not null auto_increment, order_no varchar(64) not null,
 member_id bigint not null, status varchar(24) not null default 'PENDING_PAYMENT',
 payment_status varchar(16) not null default 'UNPAID', idempotency_key varchar(80) not null,
 product_amount decimal(12,2) not null default 0, shipping_fee decimal(12,2) not null default 0,
 discount_amount decimal(12,2) not null default 0, payable_amount decimal(12,2) not null default 0,
 receiver_name varchar(50) not null, receiver_phone varchar(20) not null,
 receiver_province varchar(50) not null, receiver_city varchar(50) not null,
 receiver_district varchar(50) not null, receiver_detail_address varchar(255) not null,
 remark varchar(500), cancel_reason varchar(255), version int not null default 0,
 create_time datetime not null, pay_time datetime, close_time datetime,
 update_time datetime not null,
 primary key(order_id), unique key uk_mall_order_no(order_no),
 unique key uk_mall_order_member_idempotency(member_id,idempotency_key),
 key idx_mall_order_member_status(member_id,status,create_time),
 key idx_mall_order_status_time(status,create_time),
 constraint ck_mall_order_amounts check(product_amount>=0 and shipping_fee>=0 and discount_amount>=0 and payable_amount>=0),
 constraint ck_mall_order_status check(status in ('PENDING_PAYMENT','RISK_REVIEW','PENDING_SHIPMENT','SHIPPED','COMPLETED','AFTER_SALE','CANCELLED','CLOSED')),
 constraint ck_mall_order_payment_status check(payment_status in ('UNPAID','PAYING','PAID','FAILED','REFUNDING','REFUNDED'))
) engine=InnoDB default charset=utf8mb4 comment='商城订单';

create table if not exists mall_order_item (
 order_item_id bigint not null auto_increment, order_id bigint not null, sku_id bigint,
 sku_code varchar(64) not null, product_name varchar(200) not null, sku_name varchar(200),
 product_image varchar(255), unit_price decimal(12,2) not null, quantity int not null,
 line_amount decimal(12,2) not null, create_time datetime not null,
 primary key(order_item_id), key idx_mall_order_item_order(order_id), key idx_mall_order_item_sku(sku_id),
 constraint ck_mall_order_item_price check(unit_price>=0 and line_amount>=0),
 constraint ck_mall_order_item_quantity check(quantity>0)
) engine=InnoDB default charset=utf8mb4 comment='商城订单商品快照';

create table if not exists mall_order_operation_log (
 operation_id bigint not null auto_increment, order_id bigint not null, order_no varchar(64) not null,
 from_status varchar(24), to_status varchar(24) not null, operator_type varchar(16) not null,
 operator_id varchar(64), remark varchar(500), request_id varchar(64), create_time datetime not null,
 primary key(operation_id), key idx_mall_order_operation(order_id,operation_id), key idx_mall_order_operation_no(order_no),
 constraint ck_mall_order_operation_type check(operator_type in ('MEMBER','ADMIN','SYSTEM','PAYMENT','RISK'))
) engine=InnoDB default charset=utf8mb4 comment='商城订单操作日志';
