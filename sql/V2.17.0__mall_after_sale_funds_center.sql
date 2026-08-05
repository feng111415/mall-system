-- Mall V0.4 module 5: after-sale funds and exception operations center.
set names utf8mb4;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4000,convert(0xe594aee5908ee8b584e98791e5a484e79086e4b8ade5bf83 using utf8mb4),3982,1,'center','mall/after-sale-funds/index',null,'MallAfterSaleFundsCenter',1,0,'C','0','0','mall:after-sale-funds:center:list','money','mall_v0.4.5',now(),convert(0xe594aee5908ee5aea1e6a0b8e38081e98080e6acbee38081e5afb9e8b4a6e5918ae8ada6e4b88ee8a1a5e581bfe4bbbbe58aa1e7bb9fe4b880e585a5e58fa3 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4000 or perms='mall:after-sale-funds:center:list');
update sys_menu set menu_name='售后资金处理中心',parent_id=3982,order_num=1,path='center',component='mall/after-sale-funds/index',route_name='MallAfterSaleFundsCenter',menu_type='C',visible='0',status='0',perms='mall:after-sale-funds:center:list',icon='money' where menu_id=4000;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4001,convert(0xe594aee5908ee8b584e98791e4b8ade5bf83e69fa5e8afa2 using utf8mb4),4000,1,'','','','',1,0,'F','0','0','mall:after-sale-funds:center:query','#','mall_v0.4.5',now(),convert(0xe68c89e5b297e4bd8de8819ae59088e594aee5908ee5928ce8b584e98791e5bc82e5b8b8e5b7a5e4bd9ce9a1b9 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4001 or perms='mall:after-sale-funds:center:query');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4002,convert(0xe8a1a5e581bfe4bbbbe58aa1e69fa5e8afa2 using utf8mb4),4000,2,'','','','',1,0,'F','0','0','mall:compensation:list','#','mall_v0.4.5',now(),convert(0xe69fa5e79c8be694afe4bb98e38081e98080e6acbee5928ce5ba93e5ad98e8a1a5e581bfe4bbbbe58aa1 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4002 or perms='mall:compensation:list');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4003,convert(0xe8a1a5e581bfe4bbbbe58aa1e9878de8af95 using utf8mb4),4000,3,'','','','',1,0,'F','0','0','mall:compensation:retry','#','mall_v0.4.5',now(),convert(0xe9878de7bdaee5a4b1e8b4a5e68896e4babae5b7a5e8a1a5e581bfe4bbbbe58aa1e5b9b6e7ad89e5be85e9878de696b0e689a7e8a18c using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4003 or perms='mall:compensation:retry');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4004,convert(0xe6898be58aa8e689a7e8a18ce8a1a5e581bf using utf8mb4),4000,4,'','','','',1,0,'F','0','0','mall:compensation:run','#','mall_v0.4.5',now(),convert(0xe6898be58aa8e8a7a6e58f91e588b0e69c9fe8a1a5e581bfe4bbbbe58aa1efbc8ce4bb85e8bf90e890a5e4b8bbe7aea1e4bdbfe794a8 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4004 or perms='mall:compensation:run');

update sys_menu set order_num=2 where menu_id=3970 and parent_id=3982;
update sys_menu set order_num=3 where menu_id=3900 and parent_id=3982;

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_ops_lead' and r.create_by='mall_v2.13.0' and m.menu_id in (4000,4001,4002,4003,4004);

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_customer_service' and r.create_by='mall_v2.13.0' and m.menu_id in (4000,4001);

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_finance_risk' and r.create_by='mall_v2.13.0' and m.menu_id in (4000,4001,4002,4003);
