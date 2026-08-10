-- Rollback V2.25.0. Run only against the migration verification database.
set names utf8mb4;

delete from sys_role_menu where menu_id=4040;
delete from sys_menu where menu_id=4040 and perms='mall:analytics:list';

update sys_menu set order_num=1 where menu_id=3980;
update sys_menu set order_num=2 where menu_id=3981;
update sys_menu set order_num=3 where menu_id=3982;
update sys_menu set order_num=4 where menu_id=3983;
update sys_menu set order_num=5 where menu_id=4010;
update sys_menu set order_num=6 where menu_id=4020;
update sys_menu set order_num=7 where menu_id=4030;
