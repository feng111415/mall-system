-- Mall V0.4 module 3: product and inventory operations center.
set names utf8mb4;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3993,convert(0xe59586e59381e5ba93e5ad98e4b8ade5bf83 using utf8mb4),3980,1,'product-inventory-center','mall/product-inventory/index',null,'MallProductInventoryCenter',1,0,'C','0','0','mall:product-inventory:list','goods','mall_v0.4.3',now(),convert(0xe59586e59381e5ba93e5ad98e4b88ee58fafe594aee78ab6e68081 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=3993 or perms='mall:product-inventory:list');
update sys_menu set menu_name=convert(0xe59586e59381e5ba93e5ad98e4b8ade5bf83 using utf8mb4),parent_id=3980,order_num=1,path='product-inventory-center',component='mall/product-inventory/index',route_name='MallProductInventoryCenter',menu_type='C',visible='0',status='0',perms='mall:product-inventory:list',icon='goods' where menu_id=3993;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3994,convert(0xe59586e59381e5ba93e5ad98e4b8ade5bf83e69fa5e8afa2 using utf8mb4),3993,1,'','','','',1,0,'F','0','0','mall:product-inventory:query','#','mall_v0.4.3',now(),'Product inventory center detail read permission'
where not exists(select 1 from sys_menu where menu_id=3994 or perms='mall:product-inventory:query');
update sys_menu set menu_name=convert(0xe59586e59381e5ba93e5ad98e4b8ade5bf83e69fa5e8afa2 using utf8mb4),parent_id=3993,menu_type='F',visible='0',status='0',perms='mall:product-inventory:query' where menu_id=3994;

-- Keep the center first while preserving the existing product and inventory entries.
update sys_menu set order_num=2 where menu_id=3002 and parent_id=3980;
update sys_menu set order_num=3 where menu_id=3003 and parent_id=3980;
update sys_menu set order_num=4 where menu_id=3004 and parent_id=3980;
update sys_menu set order_num=5 where menu_id=3050 and parent_id=3980;

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key in ('mall_ops_lead','mall_product_ops','mall_fulfillment')
  and r.create_by='mall_v2.13.0' and m.menu_id in (3993,3994);
