-- Roll back Mall V0.4 module 2 order operations center.
set names utf8mb4;

delete rm from sys_role_menu rm where rm.menu_id in (3991,3992);
delete from sys_menu where menu_id in (3992,3991) and create_by='mall_v0.4.2';
update sys_menu set order_num=1 where menu_id=3960 and parent_id=3981;
