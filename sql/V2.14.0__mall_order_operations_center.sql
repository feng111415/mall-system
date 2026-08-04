-- Mall V0.4 module 2: unified order operations center.
set names utf8mb4;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3991,convert(0xe8aea2e58d95e8bf90e890a5e4b8ade5bf83 using utf8mb4),3981,1,'order-center','mall/order-center/index',null,'MallOrderOperationsCenter',1,0,'C','0','0','mall:order-center:list','list','mall_v0.4.2',now(),'Unified order operations query and detail entry'
where not exists(select 1 from sys_menu where menu_id=3991 or perms='mall:order-center:list');
update sys_menu set menu_name=convert(0xe8aea2e58d95e8bf90e890a5e4b8ade5bf83 using utf8mb4),parent_id=3981,order_num=1,path='order-center',component='mall/order-center/index',route_name='MallOrderOperationsCenter',menu_type='C',visible='0',status='0',perms='mall:order-center:list',icon='list' where menu_id=3991;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3992,convert(0xe8aea2e58d95e8bf90e890a5e4b8ade5bf83e8afa6e68385e69fa5e8afa2 using utf8mb4),3991,1,'','','','',1,0,'F','0','0','mall:order-center:query','#','mall_v0.4.2',now(),'Order operations detail read permission'
where not exists(select 1 from sys_menu where menu_id=3992 or perms='mall:order-center:query');
update sys_menu set menu_name=convert(0xe8aea2e58d95e8bf90e890a5e4b8ade5bf83e8afa6e68385e69fa5e8afa2 using utf8mb4),parent_id=3991,menu_type='F',visible='0',status='0',perms='mall:order-center:query' where menu_id=3992;

-- Operations lead and all operational read roles can use the center; product operations stays outside this workflow.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key in ('mall_ops_lead','mall_customer_service','mall_fulfillment','mall_finance_risk')
  and r.create_by='mall_v2.13.0' and m.menu_id in (3991,3992);

update sys_menu set order_num=2 where menu_id=3960 and parent_id=3981;
