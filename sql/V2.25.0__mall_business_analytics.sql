-- Mall V0.5 module 7: role-scoped business analytics center.
set names utf8mb4;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4040,convert(0xe7bb8fe890a5e58886e69e90e4b8ade5bf83 using utf8mb4),3000,1,'business-analytics','mall/business-analytics/index',null,'MallBusinessAnalytics',1,0,'C','0','0','mall:analytics:list','chart','mall_v2.25.0',now(),convert(0xe58faae68f90e4be9be5b297e4bd8de8818ce8b4a3e58685e79a84e59586e59f8ee7bb8fe890a5e8819ae59088e695b0e68dae using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4040 or perms='mall:analytics:list');

update sys_menu set menu_name=convert(0xe7bb8fe890a5e58886e69e90e4b8ade5bf83 using utf8mb4),parent_id=3000,order_num=1,
 path='business-analytics',component='mall/business-analytics/index',route_name='MallBusinessAnalytics',menu_type='C',visible='0',status='0',
 perms='mall:analytics:list',icon='chart',remark=convert(0xe58faae68f90e4be9be5b297e4bd8de8818ce8b4a3e58685e79a84e59586e59f8ee7bb8fe890a5e8819ae59088e695b0e68dae using utf8mb4)
where menu_id=4040;

-- Keep the analytics entry directly below the responsibility page.
update sys_menu set order_num=2 where menu_id=3980;
update sys_menu set order_num=3 where menu_id=3981;
update sys_menu set order_num=4 where menu_id=3982;
update sys_menu set order_num=5 where menu_id=3983;
update sys_menu set order_num=6 where menu_id=4010;
update sys_menu set order_num=7 where menu_id=4020;
update sys_menu set order_num=8 where menu_id=4030;

-- All five operational roles can enter the center; response sections are reduced by role key in the service.
insert ignore into sys_role_menu(role_id,menu_id)
select role_id,4040 from sys_role
where role_key in ('mall_ops_lead','mall_product_ops','mall_customer_service','mall_fulfillment','mall_finance_risk')
  and del_flag='0';
