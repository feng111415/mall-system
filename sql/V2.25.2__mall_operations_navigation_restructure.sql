-- Apply the approved C navigation: workbench first, business domains collapsed.
set names utf8mb4;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4050,convert(0xe8bf90e890a5e5b7a5e4bd9ce58fb0 using utf8mb4),3000,0,'overview',null,null,'MallOperationsOverview',1,0,'M','0','0','','dashboard','mall_v2.25.2',now(),convert(0xe4bb8ae697a5e4bbbbe58aa1e4b88ee5bc82e5b8b8e8819ae59088 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4050 or (parent_id=3000 and path='overview'));

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4051,convert(0xe8bf90e890a5e9a696e9a1b5 using utf8mb4),4050,1,'home','mall/operations/home',null,'MallOperationsHome',1,0,'C','0','0','','dashboard','mall_v2.25.2',now(),convert(0xe59586e59f8ee8bf90e890a5e9a696e9a1b5e4b88ee5b297e4bd8de4bbbbe58aa1e8819ae59088 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4051 or (parent_id=4050 and path='home'));

update sys_menu set menu_name=convert(0xe8bf90e890a5e5b7a5e4bd9ce58fb0 using utf8mb4),parent_id=3000,order_num=0,
 path='overview',component=null,route_name='MallOperationsOverview',menu_type='M',visible='0',status='0',perms='',icon='dashboard'
where menu_id=4050;
update sys_menu set menu_name=convert(0xe8bf90e890a5e9a696e9a1b5 using utf8mb4),parent_id=4050,order_num=1,
 path='home',component='mall/operations/home',route_name='MallOperationsHome',menu_type='C',visible='0',status='0',perms='',icon='dashboard'
where menu_id=4051;

update sys_menu set menu_name=convert(0xe7bb8fe890a5e58886e69e90 using utf8mb4),parent_id=4050,order_num=2,
 path='analytics',route_name='MallBusinessAnalytics',icon='' where menu_id=4040;
update sys_menu set menu_name=convert(0xe5b297e4bd8de8818ce8b4a3 using utf8mb4),parent_id=4050,order_num=3,
 path='responsibilities',route_name='MallOperationsRoles',icon='' where menu_id=3990;

-- Merge the three single-purpose groups into one content and marketing domain.
update sys_menu set menu_name=convert(0xe58685e5aeb9e4b88ee890a5e99480 using utf8mb4),parent_id=3000,order_num=5,
 path='content-marketing',route_name='MallContentMarketing',visible='0',status='0',icon='edit',
 remark=convert(0xe59586e59f8ee58685e5aeb9e38081e4bc98e683a0e588b8e4b88ee8af84e4bbb7e8bf90e890a5 using utf8mb4)
where menu_id=4010;
update sys_menu set menu_name=convert(0xe59586e59f8ee9a696e9a1b5e9858de7bdae using utf8mb4),parent_id=4010,order_num=1,path='homepage' where menu_id=4011;
update sys_menu set parent_id=4010,order_num=2,path='coupons' where menu_id=4021;
update sys_menu set parent_id=4010,order_num=3,path='reviews' where menu_id=4031;

-- Retain legacy directory IDs for rollback, but remove them from active navigation.
update sys_menu set visible='1',status='1' where menu_id in (4020,4030);

update sys_menu set order_num=1 where menu_id=3980;
update sys_menu set order_num=2 where menu_id=3981;
update sys_menu set order_num=3 where menu_id=3982;
update sys_menu set order_num=4 where menu_id=3983;

-- All roles that previously had responsibilities or analytics receive the new ancestor and home leaf.
insert ignore into sys_role_menu(role_id,menu_id)
select distinct role_id,4050 from sys_role_menu where menu_id in (3990,4040);
insert ignore into sys_role_menu(role_id,menu_id)
select distinct role_id,4051 from sys_role_menu where menu_id in (3990,4040);

-- The merged directory is only an ancestor; child permissions remain unchanged.
insert ignore into sys_role_menu(role_id,menu_id)
select distinct role_id,4010 from sys_role_menu where menu_id in (4011,4021,4031);
delete from sys_role_menu where menu_id in (4020,4030);
