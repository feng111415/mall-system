-- Roll back V2.9.0 in an isolated verification database only.
delete from sys_job where job_group='SYSTEM' and invoke_target='mallReceiptTask.autoConfirmReceipts';
delete from sys_menu where menu_id in (3960,3961,3962,3963) or perms in ('mall:logistics:list','mall:logistics:query','mall:logistics:ship','mall:logistics:node');
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
set @mall_node_status_sql := if(@mall_node_status_constraint_exists = 1,
    'alter table mall_logistics_node drop check ck_mall_logistics_node_status', 'select 1');
prepare mall_node_status_stmt from @mall_node_status_sql; execute mall_node_status_stmt; deallocate prepare mall_node_status_stmt;

set @mall_node_status_index_exists := (
    select count(*) from information_schema.statistics
    where table_schema=database() and table_name='mall_logistics_node'
      and index_name='idx_mall_logistics_node_status_time'
);
set @mall_node_status_index_sql := if(@mall_node_status_index_exists = 1,
    'alter table mall_logistics_node drop index idx_mall_logistics_node_status_time', 'select 1');
prepare mall_node_status_index_stmt from @mall_node_status_index_sql; execute mall_node_status_index_stmt; deallocate prepare mall_node_status_index_stmt;
