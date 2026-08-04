set names utf8mb4;

set @mall_fulfillment_company_index_exists := (
    select count(*) from information_schema.statistics
    where table_schema=database() and table_name='mall_logistics_shipment'
      and index_name='idx_mall_logistics_company_time'
);
set @mall_fulfillment_company_index_sql := if(@mall_fulfillment_company_index_exists > 0,
    'alter table mall_logistics_shipment drop index idx_mall_logistics_company_time',
    'select 1');
prepare mall_fulfillment_company_index_stmt from @mall_fulfillment_company_index_sql;
execute mall_fulfillment_company_index_stmt;
deallocate prepare mall_fulfillment_company_index_stmt;

set @mall_fulfillment_status_index_exists := (
    select count(*) from information_schema.statistics
    where table_schema=database() and table_name='mall_logistics_shipment'
      and index_name='idx_mall_logistics_status_time'
);
set @mall_fulfillment_status_index_sql := if(@mall_fulfillment_status_index_exists > 0,
    'alter table mall_logistics_shipment drop index idx_mall_logistics_status_time',
    'select 1');
prepare mall_fulfillment_status_index_stmt from @mall_fulfillment_status_index_sql;
execute mall_fulfillment_status_index_stmt;
deallocate prepare mall_fulfillment_status_index_stmt;

update sys_menu
set menu_name=convert(0xe58f91e8b4a7e5b7a5e4bd9ce58fb0 using utf8mb4),
    remark=convert(0xe59586e59f8ee6898be5b7a5e789a9e6b581e4b88ee694b6e8b4a7 using utf8mb4)
where menu_id=3960 or perms='mall:logistics:list';
