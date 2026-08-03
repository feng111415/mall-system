-- Roll back Mall V0.4 module 1 information architecture and generated roles.
set names utf8mb4;

-- Remove only roles created by this migration. Any manually assigned users are detached first.
delete ur from sys_user_role ur
join sys_role r on r.role_id=ur.role_id
where r.create_by='mall_v2.13.0' and r.role_key in ('mall_ops_lead','mall_product_ops','mall_customer_service','mall_fulfillment','mall_finance_risk');
delete rm from sys_role_menu rm
join sys_role r on r.role_id=rm.role_id
where r.create_by='mall_v2.13.0' and r.role_key in ('mall_ops_lead','mall_product_ops','mall_customer_service','mall_fulfillment','mall_finance_risk');
delete from sys_role
where create_by='mall_v2.13.0' and role_key in ('mall_ops_lead','mall_product_ops','mall_customer_service','mall_fulfillment','mall_finance_risk');

-- Restore the V0.3 flat mall menu without changing functional IDs, routes or permissions.
update sys_menu set parent_id=3000,order_num=1,menu_name=convert(0xe4bc9ae59198e7aea1e79086 using utf8mb4) where menu_id=3001;
update sys_menu set parent_id=3000,order_num=2 where menu_id=3002;
update sys_menu set parent_id=3000,order_num=3 where menu_id=3003;
update sys_menu set parent_id=3000,order_num=4 where menu_id=3004;
update sys_menu set parent_id=3000,order_num=5 where menu_id=3050;
update sys_menu set parent_id=3000,order_num=6,menu_name=convert(0xe789a9e6b581e5b1a5e7baa6 using utf8mb4),icon='logistics' where menu_id=3960;
update sys_menu set parent_id=3000,order_num=7,menu_name=convert(0xe8aea2e58d95e9a1b9e594aee5908e using utf8mb4),icon='refund' where menu_id=3970;
update sys_menu set parent_id=3000,order_num=10 where menu_id=3900;

delete from sys_role_menu where menu_id in (3973,3980,3981,3982,3983,3990);
delete from sys_menu where menu_id=3973 and create_by='mall_v2.13.0';
delete from sys_menu where menu_id in (3980,3981,3982,3983) and create_by='mall_v2.13.0';
delete from sys_menu where menu_id=3990 and create_by='mall_v2.13.0';
