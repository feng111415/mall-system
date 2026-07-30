-- Mall V0.3 module 3: item-level after-sale.
set names utf8mb4;

create table if not exists mall_item_after_sale (
 after_sale_id bigint not null auto_increment, after_sale_no varchar(64) not null, order_id bigint not null, order_no varchar(64) not null, member_id bigint not null,
 type varchar(24) not null, reason_code varchar(32) not null, reason varchar(255), evidence_url varchar(1000), status varchar(24) not null,
 refund_amount decimal(12,2) not null default 0, shipping_refund_amount decimal(12,2) not null default 0, return_company_code varchar(32), return_tracking_no varchar(128),
 risk_signal varchar(500), failure_reason varchar(255), provider_refund_no varchar(128), deadline_time datetime not null, refund_time datetime, create_time datetime not null, update_time datetime not null,
 primary key(after_sale_id), unique key uk_mall_item_after_sale_no(after_sale_no), key idx_mall_item_after_sale_member(member_id,create_time), key idx_mall_item_after_sale_order(order_id),
 constraint ck_mall_item_after_sale_type check(type in ('ONLY_REFUND','RETURN_REFUND')),
 constraint ck_mall_item_after_sale_status check(status in ('PENDING_REVIEW','APPROVED','RETURN_SHIPPED','REFUNDING','SUCCESS','REJECTED','FAILED')),
 constraint ck_mall_item_after_sale_amount check(refund_amount>=0 and shipping_refund_amount>=0)
) engine=InnoDB default charset=utf8mb4 comment='商城订单项售后单';
create table if not exists mall_item_after_sale_item (
 after_sale_item_id bigint not null auto_increment, after_sale_id bigint not null, order_item_id bigint not null, sku_id bigint, product_name varchar(200) not null, sku_name varchar(200), order_quantity int not null, requested_quantity int not null, unit_price decimal(12,2) not null, refund_amount decimal(12,2) not null,
 primary key(after_sale_item_id), unique key uk_mall_item_after_sale_item(after_sale_id,order_item_id), key idx_mall_item_after_sale_item_order(order_item_id),
 constraint ck_mall_item_after_sale_item_quantity check(order_quantity>0 and requested_quantity>0 and requested_quantity<=order_quantity), constraint ck_mall_item_after_sale_item_amount check(unit_price>=0 and refund_amount>=0)
) engine=InnoDB default charset=utf8mb4 comment='商城订单项售后明细';

alter table mall_item_after_sale comment = '商城订单项售后单';
alter table mall_item_after_sale_item comment = '商城订单项售后明细';

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3970,convert(0xe8aea2e58d95e9a1b9e594aee5908e using utf8mb4),3000,7,'after-sale','mall/after-sale/index',null,'',1,0,'C','0','0','mall:after-sale:list','refund','system',now(),convert(0xe59586e59f8ee8aea2e58d95e9a1b9e594aee5908ee5aea1e6a0b8 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3970 or perms='mall:after-sale:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3971,convert(0xe594aee5908ee69fa5e8afa2 using utf8mb4),3970,1,'',null,null,'',1,0,'F','0','0','mall:after-sale:query','#','system',now(),''
where not exists(select 1 from sys_menu where menu_id=3971 or perms='mall:after-sale:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3972,convert(0xe594aee5908ee5aea1e6a0b8 using utf8mb4),3970,2,'',null,null,'',1,0,'F','0','0','mall:after-sale:audit','#','system',now(),''
where not exists(select 1 from sys_menu where menu_id=3972 or perms='mall:after-sale:audit');

update sys_menu set menu_name=convert(0xe8aea2e58d95e9a1b9e594aee5908e using utf8mb4), remark=convert(0xe59586e59f8ee8aea2e58d95e9a1b9e594aee5908ee5aea1e6a0b8 using utf8mb4)
where menu_id=3970 or perms='mall:after-sale:list';
update sys_menu set menu_name=convert(0xe594aee5908ee69fa5e8afa2 using utf8mb4)
where menu_id=3971 or perms='mall:after-sale:query';
update sys_menu set menu_name=convert(0xe594aee5908ee5aea1e6a0b8 using utf8mb4)
where menu_id=3972 or perms='mall:after-sale:audit';
