-- Admin menu and permissions for reconciliation differences and alerts.
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3060,'对账与告警',3000,10,'reconciliation','mall/reconciliation/index',null,'MallReconciliation',1,0,'C','0','0','mall:reconciliation:list','monitor','system',now(),'商城支付退款对账与差异告警'
where not exists(select 1 from sys_menu where menu_id=3060 or perms='mall:reconciliation:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3061,'对账检查',3060,1,'',null,null,'',1,0,'F','0','0','mall:reconciliation:check','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:reconciliation:check');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3062,'差异处理',3060,2,'',null,null,'',1,0,'F','0','0','mall:reconciliation:handle','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:reconciliation:handle');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3063,'告警查询',3060,3,'',null,null,'',1,0,'F','0','0','mall:reconciliation:alert:list','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:reconciliation:alert:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3064,'确认告警',3060,4,'',null,null,'',1,0,'F','0','0','mall:reconciliation:alert:ack','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:reconciliation:alert:ack');
