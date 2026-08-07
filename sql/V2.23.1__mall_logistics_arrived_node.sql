-- Add ARRIVED as a non-terminal logistics node between out-for-delivery and signed.
set names utf8mb4;

set @mall_arrived_old_constraint := (
    select count(*) from information_schema.table_constraints
    where constraint_schema=database() and table_name='mall_logistics_node'
      and constraint_name='ck_mall_logistics_node_status' and constraint_type='CHECK'
);
set @mall_arrived_drop_sql := if(@mall_arrived_old_constraint > 0,
    'alter table mall_logistics_node drop check ck_mall_logistics_node_status',
    'select 1');
prepare mall_arrived_drop_stmt from @mall_arrived_drop_sql;
execute mall_arrived_drop_stmt;
deallocate prepare mall_arrived_drop_stmt;

alter table mall_logistics_node
    add constraint ck_mall_logistics_node_status
    check(node_status in ('SHIPPED','IN_TRANSIT','OUT_FOR_DELIVERY','ARRIVED','DELIVERED','EXCEPTION','CORRECTION'));
