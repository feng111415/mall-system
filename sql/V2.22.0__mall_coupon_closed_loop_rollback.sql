-- Rollback for V2.22.0. Run only against the migration verification database.
set names utf8mb4;
delete from sys_role_menu where menu_id between 4020 and 4026;
delete from sys_menu where menu_id between 4020 and 4026;
drop table if exists mall_coupon_operation_audit;
drop table if exists mall_order_coupon;
drop table if exists mall_member_coupon;
drop table if exists mall_coupon_scope;
drop table if exists mall_coupon_template;
