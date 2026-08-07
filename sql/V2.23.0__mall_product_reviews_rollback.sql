-- Rollback for V2.23.0. Run only against the migration verification database.
set names utf8mb4;
delete from sys_role_menu where menu_id between 4030 and 4033;
delete from sys_menu where menu_id between 4030 and 4033;
drop table if exists mall_product_review_image;
drop table if exists mall_product_review;
