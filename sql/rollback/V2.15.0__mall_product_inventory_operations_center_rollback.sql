set names utf8mb4;

delete rm from sys_role_menu rm where rm.menu_id in (3993,3994);
delete from sys_menu where menu_id in (3994,3993);

update sys_menu set order_num=1 where menu_id=3002 and parent_id=3980;
update sys_menu set order_num=2 where menu_id=3003 and parent_id=3980;
update sys_menu set order_num=3 where menu_id=3004 and parent_id=3980;
update sys_menu set order_num=4 where menu_id=3050 and parent_id=3980;
