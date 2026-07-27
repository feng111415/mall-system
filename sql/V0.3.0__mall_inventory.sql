-- Mall phase three: inventory source of truth and append-only audit records.
create table if not exists mall_stock (
 stock_id bigint not null auto_increment, sku_id bigint not null,
 available_quantity int not null default 0, locked_quantity int not null default 0,
 sold_quantity int not null default 0, warning_threshold int not null default 0,
 create_time datetime not null, update_time datetime not null,
 primary key(stock_id), unique key uk_mall_stock_sku(sku_id),
 constraint ck_mall_stock_available check(available_quantity>=0),
 constraint ck_mall_stock_locked check(locked_quantity>=0),
 constraint ck_mall_stock_sold check(sold_quantity>=0),
 constraint ck_mall_stock_warning check(warning_threshold>=0)
) engine=InnoDB default charset=utf8mb4 comment='商城库存快照';

create table if not exists mall_stock_reservation (
 reservation_id bigint not null auto_increment, order_no varchar(64) not null,
 sku_id bigint not null, quantity int not null, status varchar(16) not null,
 create_time datetime not null, update_time datetime not null,
 primary key(reservation_id), unique key uk_stock_reservation_order_sku(order_no,sku_id),
 key idx_stock_reservation_order(order_no),
 constraint ck_stock_reservation_quantity check(quantity>0)
) engine=InnoDB default charset=utf8mb4 comment='商城库存预占';

create table if not exists mall_stock_log (
 log_id bigint not null auto_increment, sku_id bigint not null,
 operation_type varchar(16) not null, source_type varchar(16) not null, source_no varchar(64),
 available_change int not null, locked_change int not null, sold_change int not null,
 available_after int not null, locked_after int not null, sold_after int not null,
 reason varchar(255) not null, operator varchar(64), create_time datetime not null,
 primary key(log_id), key idx_stock_log_sku(sku_id,log_id), key idx_stock_log_source(source_type,source_no)
) engine=InnoDB default charset=utf8mb4 comment='商城库存流水';

insert into mall_stock(sku_id,available_quantity,locked_quantity,sold_quantity,warning_threshold,create_time,update_time)
select s.sku_id,s.available_stock,0,0,0,now(),now() from mall_sku s
left join mall_stock st on st.sku_id=s.sku_id where s.del_flag='0' and st.sku_id is null;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3050,'库存管理',3000,5,'inventory',null,null,'',1,0,'C','0','0','mall:inventory:list','goods','system',now(),'商城库存与流水'
where not exists(select 1 from sys_menu where menu_id=3050 or perms='mall:inventory:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3051,'库存查询',3050,1,'',null,null,'',1,0,'F','0','0','mall:inventory:query','#','system',now(),''
where not exists(select 1 from sys_menu where perms='mall:inventory:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3052,'库存调整',3050,2,'',null,null,'',1,0,'F','0','0','mall:inventory:adjust','#','system',now(),''
where not exists(select 1 from sys_menu where perms='mall:inventory:adjust');
