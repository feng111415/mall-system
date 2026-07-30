-- Mall V0.3 module 2: manual shipment, append-only tracking and receipt confirmation.
set @mall_shipment_status_exists := (
    select count(*) from information_schema.table_constraints
    where table_schema=database() and table_name='mall_logistics_shipment'
      and constraint_name='ck_mall_logistics_status'
);
set @mall_shipment_status_sql := if(@mall_shipment_status_exists = 1,
    'alter table mall_logistics_shipment drop check ck_mall_logistics_status', 'select 1');
prepare mall_shipment_status_stmt from @mall_shipment_status_sql; execute mall_shipment_status_stmt; deallocate prepare mall_shipment_status_stmt;
set @mall_shipment_status_sql := 'alter table mall_logistics_shipment add constraint ck_mall_logistics_status check(status in (''IN_TRANSIT'',''DELIVERED'',''CANCELLED''))';
prepare mall_shipment_status_stmt from @mall_shipment_status_sql; execute mall_shipment_status_stmt; deallocate prepare mall_shipment_status_stmt;

set @mall_node_status_constraint_exists := (
    select count(*) from information_schema.table_constraints
    where table_schema=database() and table_name='mall_logistics_node'
      and constraint_name='ck_mall_logistics_node_status'
);
set @mall_node_status_sql := if(@mall_node_status_constraint_exists = 0,
    'alter table mall_logistics_node add constraint ck_mall_logistics_node_status check(node_status in (''SHIPPED'',''IN_TRANSIT'',''OUT_FOR_DELIVERY'',''DELIVERED'',''EXCEPTION'',''CORRECTION''))',
    'select 1');
prepare mall_node_status_stmt from @mall_node_status_sql; execute mall_node_status_stmt; deallocate prepare mall_node_status_stmt;

set @mall_node_status_index_exists := (
    select count(*) from information_schema.statistics
    where table_schema=database() and table_name='mall_logistics_node'
      and index_name='idx_mall_logistics_node_status_time'
);
set @mall_node_status_index_sql := if(@mall_node_status_index_exists = 0,
    'alter table mall_logistics_node add key idx_mall_logistics_node_status_time(node_status,event_time)',
    'select 1');
prepare mall_node_status_index_stmt from @mall_node_status_index_sql; execute mall_node_status_index_stmt; deallocate prepare mall_node_status_index_stmt;

insert into sys_job(job_name, job_group, invoke_target, cron_expression, misfire_policy,
    concurrent, status, create_by, create_time, remark)
select 'Mall receipt auto confirm', 'SYSTEM', 'mallReceiptTask.autoConfirmReceipts', '0 0/10 * * * ?', '3',
    '1', '0', 'mall-system', now(), 'Confirm delivered orders after seven days'
where not exists (
    select 1 from sys_job
    where job_group='SYSTEM' and invoke_target='mallReceiptTask.autoConfirmReceipts'
);

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3960,'物流履约',3000,6,'logistics','mall/logistics/index',null,'',1,0,'C','0','0','mall:logistics:list','logistics','system',now(),'商城手工物流与收货'
where not exists(select 1 from sys_menu where menu_id=3960 or perms='mall:logistics:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3961,'物流查询',3960,1,'',null,null,'',1,0,'F','0','0','mall:logistics:query','#','system',now(),''
where not exists(select 1 from sys_menu where menu_id=3961 or perms='mall:logistics:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3962,'订单发货',3960,2,'',null,null,'',1,0,'F','0','0','mall:logistics:ship','#','system',now(),''
where not exists(select 1 from sys_menu where menu_id=3962 or perms='mall:logistics:ship');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3963,'追加轨迹',3960,3,'',null,null,'',1,0,'F','0','0','mall:logistics:node','#','system',now(),''
where not exists(select 1 from sys_menu where menu_id=3963 or perms='mall:logistics:node');
