-- Mall phase four: shipment records and logistics tracking nodes.
create table if not exists mall_logistics_shipment (
 shipment_id bigint not null auto_increment, order_id bigint not null,
 order_no varchar(64) not null, member_id bigint not null,
 company_code varchar(32) not null, company_name varchar(64) not null,
 tracking_no varchar(128) not null, status varchar(16) not null default 'IN_TRANSIT',
 shipped_time datetime not null, delivered_time datetime,
 create_time datetime not null, update_time datetime not null,
 primary key(shipment_id), unique key uk_mall_logistics_order(order_id),
 unique key uk_mall_logistics_tracking(tracking_no),
 key idx_mall_logistics_member(member_id,create_time),
 constraint ck_mall_logistics_status check(status in ('IN_TRANSIT','DELIVERED','CANCELLED'))
) engine=InnoDB default charset=utf8mb4 comment='商城物流运单';

create table if not exists mall_logistics_node (
 node_id bigint not null auto_increment, shipment_id bigint not null,
 tracking_no varchar(128) not null, node_status varchar(32) not null,
 title varchar(100) not null, description varchar(500) not null,
 location varchar(100), event_time datetime not null, create_time datetime not null,
 primary key(node_id), key idx_mall_logistics_node_shipment(shipment_id,event_time,node_id)
) engine=InnoDB default charset=utf8mb4 comment='商城物流轨迹节点';
