-- Rollback for V2.19.0. Run only against the migration verification database.
set names utf8mb4;

delete from sys_role_menu where menu_id in (4010,4011,4012,4013,4014,4015,4016);
delete from sys_menu where menu_id in (4016,4015,4014,4013,4012,4011,4010);
drop table if exists mall_home_content_product;
drop table if exists mall_home_content_ticker;
drop table if exists mall_home_content;
