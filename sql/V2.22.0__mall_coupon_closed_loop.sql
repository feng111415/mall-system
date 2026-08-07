-- Mall V0.5 module 4: fixed-amount coupon closed loop.
set names utf8mb4;
create table if not exists mall_coupon_template (
 coupon_id bigint not null auto_increment,coupon_name varchar(120) not null,scope_type varchar(16) not null default 'ALL',
 threshold_amount decimal(12,2) not null default 0,discount_amount decimal(12,2) not null,total_quantity int not null,
 claimed_quantity int not null default 0,per_member_limit int not null default 1,status varchar(16) not null default 'DRAFT',
 valid_from datetime not null,valid_to datetime not null,remark varchar(500),version int not null default 0,
 create_by varchar(64),create_time datetime not null,update_by varchar(64),update_time datetime,
 primary key(coupon_id),key idx_coupon_template_status_time(status,valid_from,valid_to),
 constraint ck_coupon_template_scope check(scope_type in ('ALL','CATEGORY','PRODUCT')),
 constraint ck_coupon_template_status check(status in ('DRAFT','PUBLISHED','PAUSED','ENDED')),
 constraint ck_coupon_template_amount check(threshold_amount>=0 and discount_amount>0 and discount_amount<=threshold_amount),
 constraint ck_coupon_template_quantity check(total_quantity>0 and claimed_quantity>=0 and claimed_quantity<=total_quantity and per_member_limit=1),
 constraint ck_coupon_template_time check(valid_to>valid_from)
) engine=InnoDB default charset=utf8mb4 comment='Mall coupon template';
create table if not exists mall_coupon_scope (
 scope_id bigint not null auto_increment,coupon_id bigint not null,target_id bigint not null,primary key(scope_id),
 unique key uk_coupon_scope_target(coupon_id,target_id),key idx_coupon_scope_coupon(coupon_id)
) engine=InnoDB default charset=utf8mb4 comment='Mall coupon scope';
create table if not exists mall_member_coupon (
 member_coupon_id bigint not null auto_increment,coupon_id bigint not null,member_id bigint not null,
 status varchar(16) not null default 'AVAILABLE',lock_order_id bigint,used_order_id bigint,receive_time datetime not null,
 lock_time datetime,used_time datetime,update_time datetime,primary key(member_coupon_id),
 unique key uk_member_coupon_once(coupon_id,member_id),key idx_member_coupon_member_status(member_id,status,receive_time),
 key idx_member_coupon_lock_order(lock_order_id),key idx_member_coupon_used_order(used_order_id),
 constraint ck_member_coupon_status check(status in ('AVAILABLE','LOCKED','USED','EXPIRED','REVOKED'))
) engine=InnoDB default charset=utf8mb4 comment='Mall member coupon';
create table if not exists mall_order_coupon (
 order_coupon_id bigint not null auto_increment,order_id bigint not null,order_no varchar(64) not null,
 member_coupon_id bigint not null,coupon_id bigint not null,coupon_name varchar(120) not null,scope_type varchar(16) not null,
 threshold_amount decimal(12,2) not null,discount_amount decimal(12,2) not null,status varchar(16) not null default 'LOCKED',
 create_time datetime not null,update_time datetime,primary key(order_coupon_id),unique key uk_order_coupon_order(order_id),
 unique key uk_order_coupon_member_coupon(member_coupon_id),key idx_order_coupon_no(order_no),
 constraint ck_order_coupon_status check(status in ('LOCKED','USED','RELEASED'))
) engine=InnoDB default charset=utf8mb4 comment='Mall order coupon snapshot';
create table if not exists mall_coupon_operation_audit (
 audit_id bigint not null auto_increment,coupon_id bigint not null,action_type varchar(24) not null,operator_id varchar(64) not null,
 detail varchar(500),create_time datetime not null,primary key(audit_id),key idx_coupon_audit_coupon_time(coupon_id,create_time),
 constraint ck_coupon_audit_action check(action_type in ('CREATE','UPDATE','PUBLISH','PAUSE','ISSUE'))
) engine=InnoDB default charset=utf8mb4 comment='Mall coupon operation audit';

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4020,'Coupon Operations',3000,6,'coupon',null,null,'MallCoupon',1,0,'M','0','0','','ticket','mall_v2.22.0',now(),'Coupon configuration and issuance'
where not exists(select 1 from sys_menu where menu_id=4020 or path='coupon' and parent_id=3000);
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4021,'Coupon Management',4020,1,'manage','mall/coupon/index',null,'MallCouponManage',1,0,'C','0','0','mall:coupon:list','ticket','mall_v2.22.0',now(),'Coupon closed loop operations'
where not exists(select 1 from sys_menu where menu_id=4021 or perms='mall:coupon:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4022,'Query',4021,1,'','',null,'',1,0,'F','0','0','mall:coupon:query','#','mall_v2.22.0',now(),'Query coupons and claims'
where not exists(select 1 from sys_menu where menu_id=4022 or perms='mall:coupon:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4023,'Create',4021,2,'','',null,'',1,0,'F','0','0','mall:coupon:add','#','mall_v2.22.0',now(),'Create coupon draft'
where not exists(select 1 from sys_menu where menu_id=4023 or perms='mall:coupon:add');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4024,'Edit',4021,3,'','',null,'',1,0,'F','0','0','mall:coupon:edit','#','mall_v2.22.0',now(),'Edit coupon draft'
where not exists(select 1 from sys_menu where menu_id=4024 or perms='mall:coupon:edit');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4025,'Publish or Pause',4021,4,'','',null,'',1,0,'F','0','0','mall:coupon:publish','#','mall_v2.22.0',now(),'Publish or pause coupon'
where not exists(select 1 from sys_menu where menu_id=4025 or perms='mall:coupon:publish');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4026,'Manual Issue',4021,5,'','',null,'',1,0,'F','0','0','mall:coupon:issue','#','mall_v2.22.0',now(),'Issue coupon to member'
where not exists(select 1 from sys_menu where menu_id=4026 or perms='mall:coupon:issue');
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key in ('mall_ops_lead','mall_product_ops') and m.menu_id between 4020 and 4026;
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key in ('mall_customer_service','mall_finance_risk') and m.menu_id in (4020,4021,4022);
