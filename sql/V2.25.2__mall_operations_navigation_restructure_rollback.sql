-- Roll back the C navigation to the V2.25.1 menu tree.
set names utf8mb4;

insert ignore into sys_role_menu(role_id,menu_id)
select distinct role_id,4020 from sys_role_menu where menu_id=4021;
insert ignore into sys_role_menu(role_id,menu_id)
select distinct role_id,4030 from sys_role_menu where menu_id=4031;

delete rm from sys_role_menu rm
left join sys_role_menu child on child.role_id=rm.role_id and child.menu_id=4011
where rm.menu_id=4010 and child.role_id is null;

delete from sys_role_menu where menu_id in (4050,4051);
delete from sys_menu where menu_id=4051 and create_by='mall_v2.25.2';
delete from sys_menu where menu_id=4050 and create_by='mall_v2.25.2';

update sys_menu set menu_name=convert(0xe8bf90e890a5e8818ce8b4a3 using utf8mb4),parent_id=3000,order_num=0,
 path='operations-roles',route_name='MallOperationsRoles',visible='0',status='0',icon='peoples' where menu_id=3990;
update sys_menu set menu_name=convert(0xe7bb8fe890a5e58886e69e90e4b8ade5bf83 using utf8mb4),parent_id=3000,order_num=1,
 path='business-analytics',route_name='MallBusinessAnalytics',visible='0',status='0',icon='chart' where menu_id=4040;

update sys_menu set menu_name=convert(0xe9a1b5e99da2e58685e5aeb9 using utf8mb4),parent_id=3000,order_num=6,
 path='content',route_name='MallContent',visible='0',status='0',icon='edit' where menu_id=4010;
update sys_menu set menu_name=convert(0xe9a696e9a1b5e58685e5aeb9 using utf8mb4),parent_id=4010,order_num=1,path='homepage' where menu_id=4011;
update sys_menu set parent_id=4020,order_num=1,path='manage' where menu_id=4021;
update sys_menu set parent_id=4030,order_num=1,path='manage' where menu_id=4031;
update sys_menu set parent_id=3000,order_num=7,visible='0',status='0' where menu_id=4020;
update sys_menu set parent_id=3000,order_num=8,visible='0',status='0' where menu_id=4030;

update sys_menu set order_num=2 where menu_id=3980;
update sys_menu set order_num=3 where menu_id=3981;
update sys_menu set order_num=4 where menu_id=3982;
update sys_menu set order_num=5 where menu_id=3983;
