-- Mall V0.4 module 1: RuoYi operations information architecture and role boundaries.
set names utf8mb4;

-- A visible responsibility matrix explains the real RBAC roles; it does not simulate role switching.
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3990,convert(0xe8bf90e890a5e8818ce8b4a3 using utf8mb4),3000,0,'operations-roles','mall/operations/index',null,'MallOperationsRoles',1,0,'C','0','0','mall:operations:roles','peoples','mall_v2.13.0',now(),convert(0xe59586e59f8ee8bf90e890a5e5b297e4bd8de8818ce8b4a3e4b88ee69d83e99990e79fa9e998b5 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3990 or perms='mall:operations:roles');

-- Stable business-domain directories under the existing mall root.
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3980,convert(0xe59586e59381e4b88ee5ba93e5ad98 using utf8mb4),3000,1,'product-inventory',null,null,'',1,0,'M','0','0','','shopping','mall_v2.13.0',now(),convert(0xe59586e59f8ee59586e59381e38081e5ba93e5ad98e4b88ee58fafe594aee78ab6e68081 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3980);
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3981,convert(0xe8aea2e58d95e4b88ee5b1a5e7baa6 using utf8mb4),3000,2,'order-fulfillment',null,null,'',1,0,'M','0','0','','warehouse','mall_v2.13.0',now(),convert(0xe59586e59f8ee8aea2e58d95e69fa5e8afa2e38081e58f91e8b4a7e4b88ee789a9e6b581e5b1a5e7baa6 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3981);
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3982,convert(0xe594aee5908ee4b88ee8b584e98791 using utf8mb4),3000,3,'after-sale-funds',null,null,'',1,0,'M','0','0','','money','mall_v2.13.0',now(),convert(0xe59586e59f8ee594aee5908ee5aea1e6a0b8e38081e98080e6acbee4b88ee8b584e98791e5bc82e5b8b8 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3982);
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3983,convert(0xe4bc9ae59198e8bf90e890a5 using utf8mb4),3000,4,'member-operations',null,null,'',1,0,'M','0','0','','user','mall_v2.13.0',now(),convert(0xe59586e59f8ee4bc9ae59198e8b584e69699e4b88ee4bc9ae8af9de8bf90e890a5 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3983);

update sys_menu set menu_name=convert(0xe59586e59381e4b88ee5ba93e5ad98 using utf8mb4),parent_id=3000,order_num=1,path='product-inventory',component=null,route_name='MallProductInventory',icon='shopping' where menu_id=3980;
update sys_menu set menu_name=convert(0xe8aea2e58d95e4b88ee5b1a5e7baa6 using utf8mb4),parent_id=3000,order_num=2,path='order-fulfillment',component=null,route_name='MallOrderFulfillment',icon='warehouse' where menu_id=3981;
update sys_menu set menu_name=convert(0xe594aee5908ee4b88ee8b584e98791 using utf8mb4),parent_id=3000,order_num=3,path='after-sale-funds',component=null,route_name='MallAfterSaleFunds',icon='money' where menu_id=3982;
update sys_menu set menu_name=convert(0xe4bc9ae59198e8bf90e890a5 using utf8mb4),parent_id=3000,order_num=4,path='member-operations',component=null,route_name='MallMemberOperations',icon='user' where menu_id=3983;

-- Refund execution is deliberately independent from customer-service review.
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3973,convert(0xe594aee5908ee98080e6acbe using utf8mb4),3970,3,'',null,null,'',1,0,'F','0','0','mall:after-sale:refund','#','mall_v2.13.0',now(),convert(0xe689a7e8a18ce5b7b2e5aea1e6a0b8e594aee5908ee98080e6acbe using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3973 or perms='mall:after-sale:refund');

-- Preserve existing role visibility after adding new directory ancestors.
insert ignore into sys_role_menu(role_id,menu_id)
select distinct rm.role_id,3980 from sys_role_menu rm
where rm.menu_id in (3002,3020,3021,3022,3003,3030,3031,3032,3004,3040,3041,3042,3043,3044,3050,3051,3052);
insert ignore into sys_role_menu(role_id,menu_id)
select distinct rm.role_id,3981 from sys_role_menu rm
where rm.menu_id in (3960,3961,3962,3963);
insert ignore into sys_role_menu(role_id,menu_id)
select distinct rm.role_id,3982 from sys_role_menu rm
where rm.menu_id in (3970,3971,3972,3900,3901,3902,3903,3904);
insert ignore into sys_role_menu(role_id,menu_id)
select distinct rm.role_id,3983 from sys_role_menu rm
where rm.menu_id in (3001,3010,3011,3012);

-- Existing non-V0.4 roles that could refund through the former audit permission retain that ability.
insert ignore into sys_role_menu(role_id,menu_id)
select rm.role_id,3973 from sys_role_menu rm
join sys_role r on r.role_id=rm.role_id
where rm.menu_id=3972 and (r.create_by is null or r.create_by<>'mall_v2.13.0');

-- Keep all existing functional menu IDs, routes and permission strings; only their grouping changes.
update sys_menu set parent_id=3980,order_num=1 where menu_id=3002;
update sys_menu set parent_id=3980,order_num=2 where menu_id=3003;
update sys_menu set parent_id=3980,order_num=3 where menu_id=3004;
update sys_menu set parent_id=3980,order_num=4 where menu_id=3050;
update sys_menu set parent_id=3981,order_num=1,menu_name=convert(0xe58f91e8b4a7e5b7a5e4bd9ce58fb0 using utf8mb4),icon='warehouse' where menu_id=3960;
update sys_menu set parent_id=3982,order_num=1,menu_name=convert(0xe594aee5908ee5a484e79086 using utf8mb4),icon='money' where menu_id=3970;
update sys_menu set parent_id=3982,order_num=2 where menu_id=3900;
update sys_menu set parent_id=3983,order_num=1,menu_name=convert(0xe4bc9ae59198e58897e8a1a8 using utf8mb4) where menu_id=3001;

-- Five operational roles. They are scoped to mall permissions and never receive RuoYi system permissions.
insert into sys_role(role_id,role_name,role_key,role_sort,data_scope,menu_check_strictly,dept_check_strictly,status,del_flag,create_by,create_time,remark)
select 3100,convert(0xe8bf90e890a5e4b8bbe7aea1 using utf8mb4),'mall_ops_lead',10,'1',1,1,'0','0','mall_v2.13.0',now(),convert(0xe58d8fe8b083e59586e59f8ee585a8e993bee8b7afefbc8ce5a48de6a0b8e8b7a8e5b297e4bd8de5bc82e5b8b8efbc9be4b88de9858de7bdaee88ba5e4be9de7b3bbe7bb9fe7baa7e69d83e99990 using utf8mb4)
where not exists(select 1 from sys_role where role_id=3100 or role_key='mall_ops_lead');
insert into sys_role(role_id,role_name,role_key,role_sort,data_scope,menu_check_strictly,dept_check_strictly,status,del_flag,create_by,create_time,remark)
select 3101,convert(0xe59586e59381e8bf90e890a5 using utf8mb4),'mall_product_ops',11,'1',1,1,'0','0','mall_v2.13.0',now(),convert(0xe7bbb4e68aa4e58886e7b1bbe38081e59381e7898ce38081e59586e59381e5928ce4b88ae4b88be69eb6efbc9be5ba93e5ad98e58faae8afbbefbc8ce4b88de58f91e8b4a7e38081e4b88de98080e6acbe using utf8mb4)
where not exists(select 1 from sys_role where role_id=3101 or role_key='mall_product_ops');
insert into sys_role(role_id,role_name,role_key,role_sort,data_scope,menu_check_strictly,dept_check_strictly,status,del_flag,create_by,create_time,remark)
select 3102,convert(0xe5aea2e69c8de594aee5908e using utf8mb4),'mall_customer_service',12,'1',1,1,'0','0','mall_v2.13.0',now(),convert(0xe69fa5e8afa2e4bc9ae59198e38081e8aea2e58d95e5928ce789a9e6b581efbc8ce5aea1e6a0b8e594aee5908eefbc9be4b88de689a7e8a18ce98080e6acbee38081e4b88de8b083e5ba93e5ad98e38081e4b88de58f91e8b4a7 using utf8mb4)
where not exists(select 1 from sys_role where role_id=3102 or role_key='mall_customer_service');
insert into sys_role(role_id,role_name,role_key,role_sort,data_scope,menu_check_strictly,dept_check_strictly,status,del_flag,create_by,create_time,remark)
select 3103,convert(0xe4bb93e582a8e5b1a5e7baa6 using utf8mb4),'mall_fulfillment',13,'1',1,1,'0','0','mall_v2.13.0',now(),convert(0xe8b083e695b4e5ba93e5ad98e38081e689a7e8a18ce58f91e8b4a7e5b9b6e7bbb4e68aa4e789a9e6b581e8bda8e8bfb9efbc9be4b88de694b9e4bc9ae59198e38081e4b88de5aea1e594aee5908e using utf8mb4)
where not exists(select 1 from sys_role where role_id=3103 or role_key='mall_fulfillment');
insert into sys_role(role_id,role_name,role_key,role_sort,data_scope,menu_check_strictly,dept_check_strictly,status,del_flag,create_by,create_time,remark)
select 3104,convert(0xe8b4a2e58aa1e9a38ee68ea7 using utf8mb4),'mall_finance_risk',14,'1',1,1,'0','0','mall_v2.13.0',now(),convert(0xe689a7e8a18ce5b7b2e5aea1e6a0b8e98080e6acbee5b9b6e5a484e79086e5afb9e8b4a6e4b88ee5918ae8ada6efbc9be4b88de694b9e59586e59381e38081e4b88de8b083e5ba93e5ad98e38081e4b88de58f91e8b4a7 using utf8mb4)
where not exists(select 1 from sys_role where role_id=3104 or role_key='mall_finance_risk');

-- Operations lead: all currently delivered mall capabilities.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_ops_lead' and r.create_by='mall_v2.13.0'
and m.menu_id in (3000,3990,3980,3981,3982,3983,3001,3010,3011,3012,3002,3020,3021,3022,3003,3030,3031,3032,3004,3040,3041,3042,3043,3044,3050,3051,3052,3960,3961,3962,3963,3970,3971,3972,3973,3900,3901,3902,3903,3904);

-- Product operations: product write access and inventory read-only access.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_product_ops' and r.create_by='mall_v2.13.0'
and m.menu_id in (3000,3990,3980,3002,3020,3021,3022,3003,3030,3031,3032,3004,3040,3041,3042,3043,3044,3050,3051);

-- Customer service: member maintenance, logistics query and after-sale review; no refund execution.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_customer_service' and r.create_by='mall_v2.13.0'
and m.menu_id in (3000,3990,3981,3960,3961,3982,3970,3971,3972,3983,3001,3010,3011);

-- Warehouse fulfillment: inventory adjustment, shipment and append-only tracking.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_fulfillment' and r.create_by='mall_v2.13.0'
and m.menu_id in (3000,3990,3980,3050,3051,3052,3981,3960,3961,3962,3963);

-- Finance and risk: after-sale refund execution plus reconciliation and alerts; no review permission.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_finance_risk' and r.create_by='mall_v2.13.0'
and m.menu_id in (3000,3990,3982,3970,3971,3973,3900,3901,3902,3903,3904);
