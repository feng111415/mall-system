set names utf8mb4;

delete rm from sys_role_menu rm where rm.menu_id in (4000,4001,4002,4003,4004);
delete from sys_menu where menu_id in (4001,4002,4003,4004,4000);

update sys_menu set order_num=1 where menu_id=3970 and parent_id=3982;
update sys_menu set order_num=2 where menu_id=3900 and parent_id=3982;
