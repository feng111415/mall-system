drop table if exists mall_item_after_sale_item;
drop table if exists mall_item_after_sale;
delete from sys_menu where menu_id in (3970,3971,3972) or perms in ('mall:after-sale:list','mall:after-sale:query','mall:after-sale:audit');
