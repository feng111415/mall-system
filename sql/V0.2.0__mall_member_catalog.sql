-- 商城第二阶段：独立会员身份与商品中心（MySQL 8.0）
create table if not exists mall_member (
 member_id bigint not null auto_increment, phone varchar(20) not null, nickname varchar(50) not null,
 avatar varchar(255), status char(1) not null default '0', register_source varchar(20), register_ip varchar(64),
 last_login_ip varchar(64), last_login_time datetime, del_flag char(1) not null default '0',
 create_by varchar(64), create_time datetime, update_by varchar(64), update_time datetime, remark varchar(500),
 primary key(member_id), unique key uk_mall_member_phone(phone), key idx_member_status(status)
) engine=InnoDB default charset=utf8mb4 comment='商城会员';
create table if not exists mall_member_consent (
 consent_id bigint not null auto_increment, member_id bigint not null, consent_type varchar(32) not null,
 consent_version varchar(32) not null, consent_ip varchar(64), consent_time datetime not null,
 primary key(consent_id), key idx_consent_member(member_id,consent_type)
) engine=InnoDB default charset=utf8mb4 comment='会员协议同意记录';
create table if not exists mall_member_address (
 address_id bigint not null auto_increment, member_id bigint not null, receiver_name varchar(50) not null,
 receiver_phone varchar(20) not null, province varchar(50) not null, city varchar(50) not null,district varchar(50) not null,
 detail_address varchar(255) not null,postal_code varchar(12),is_default char(1) not null default '0',del_flag char(1) not null default '0',
 create_time datetime,update_time datetime,primary key(address_id),key idx_address_member(member_id,del_flag)
) engine=InnoDB default charset=utf8mb4 comment='会员地址';
create table if not exists mall_sms_code (
 sms_id bigint not null auto_increment,phone varchar(20) not null,purpose varchar(32) not null,code_hash char(64) not null,
 code_salt char(32) not null,provider_request_id varchar(100),send_status varchar(16) not null,verify_attempts int not null default 0,
 used_flag char(1) not null default '0',expire_time datetime not null,used_time datetime,request_ip varchar(64),create_time datetime not null,
 primary key(sms_id),key idx_sms_phone_purpose(phone,purpose,create_time)
) engine=InnoDB default charset=utf8mb4 comment='短信验证码摘要';
create table if not exists mall_category (category_id bigint not null auto_increment,parent_id bigint not null default 0,category_name varchar(100) not null,level int not null default 1,category_path varchar(255),icon varchar(255),sort_no int not null default 0,status char(1) not null default '0',del_flag char(1) not null default '0',create_by varchar(64),create_time datetime,update_by varchar(64),update_time datetime,primary key(category_id),key idx_category_parent(parent_id,status)) engine=InnoDB default charset=utf8mb4 comment='商品分类';
create table if not exists mall_brand (brand_id bigint not null auto_increment,brand_name varchar(100) not null,logo_url varchar(255),description varchar(500),sort_no int not null default 0,status char(1) not null default '0',del_flag char(1) not null default '0',create_by varchar(64),create_time datetime,update_by varchar(64),update_time datetime,primary key(brand_id),unique key uk_brand_name(brand_name)) engine=InnoDB default charset=utf8mb4 comment='商品品牌';
create table if not exists mall_spu (spu_id bigint not null auto_increment,category_id bigint not null,brand_id bigint,spu_code varchar(64) not null,product_name varchar(200) not null,subtitle varchar(255),main_image varchar(255),detail_html text,price_min decimal(10,2) not null,price_max decimal(10,2) not null,publish_status char(1) not null default '0',sort_no int not null default 0,sales_count int not null default 0,del_flag char(1) not null default '0',create_by varchar(64),create_time datetime,update_by varchar(64),update_time datetime,primary key(spu_id),unique key uk_spu_code(spu_code),key idx_spu_catalog(category_id,brand_id,publish_status)) engine=InnoDB default charset=utf8mb4 comment='商品SPU';
create table if not exists mall_sku (sku_id bigint not null auto_increment,spu_id bigint not null,sku_code varchar(64) not null,sku_name varchar(200),spec_json json,image_url varchar(255),price decimal(10,2) not null,market_price decimal(10,2),available_stock int not null default 0,status char(1) not null default '1',del_flag char(1) not null default '0',active_sku_code varchar(64) generated always as (case when del_flag='0' then sku_code else null end) stored,create_by varchar(64),create_time datetime,update_time datetime,primary key(sku_id),unique key uk_active_sku_code(active_sku_code),key idx_sku_spu(spu_id,status,del_flag),constraint ck_sku_stock check(available_stock>=0)) engine=InnoDB default charset=utf8mb4 comment='商品SKU';
create table if not exists mall_spec (spec_id bigint not null auto_increment,category_id bigint,spec_name varchar(64) not null,sort_no int not null default 0,status char(1) not null default '0',primary key(spec_id)) engine=InnoDB default charset=utf8mb4 comment='规格';
create table if not exists mall_spec_value (value_id bigint not null auto_increment,spec_id bigint not null,value_name varchar(64) not null,sort_no int not null default 0,primary key(value_id),key idx_spec_value(spec_id)) engine=InnoDB default charset=utf8mb4 comment='规格值';
create table if not exists mall_product_media (media_id bigint not null auto_increment,spu_id bigint not null,sku_id bigint,media_type varchar(16) not null default 'IMAGE',media_url varchar(255) not null,sort_no int not null default 0,primary key(media_id),key idx_media_spu(spu_id,sort_no)) engine=InnoDB default charset=utf8mb4 comment='商品媒体';

insert into mall_category(parent_id,category_name,level,category_path,sort_no,status,del_flag,create_by,create_time)
select 0,'家居',1,'/1/',10,'0','0','system',now() where not exists(select 1 from mall_category where category_name='家居' and del_flag='0');
insert into mall_category(parent_id,category_name,level,category_path,sort_no,status,del_flag,create_by,create_time)
select 0,'数码',1,'/2/',20,'0','0','system',now() where not exists(select 1 from mall_category where category_name='数码' and del_flag='0');
insert into mall_category(parent_id,category_name,level,category_path,sort_no,status,del_flag,create_by,create_time)
select 0,'穿搭',1,'/3/',30,'0','0','system',now() where not exists(select 1 from mall_category where category_name='穿搭' and del_flag='0');
insert into mall_category(parent_id,category_name,level,category_path,sort_no,status,del_flag,create_by,create_time)
select 0,'咖啡',1,'/4/',40,'0','0','system',now() where not exists(select 1 from mall_category where category_name='咖啡' and del_flag='0');
insert into mall_brand(brand_name,description,sort_no,status,del_flag,create_by,create_time)
select '日常精选','商城自营精选品牌',10,'0','0','system',now() where not exists(select 1 from mall_brand where brand_name='日常精选');

insert into mall_spu(category_id,brand_id,spu_code,product_name,subtitle,main_image,detail_html,price_min,price_max,publish_status,sort_no,sales_count,del_flag,create_by,create_time)
select c.category_id,b.brand_id,'SPU-10001','北欧原木餐椅','白蜡木框架，适合餐厅与书房','/assets/chair.jpg','<p>实木结构，圆润边角，日常使用更安心。</p>',399,439,'1',10,86,'0','system',now()
from mall_category c join mall_brand b on b.brand_name='日常精选' where c.category_name='家居' and not exists(select 1 from mall_spu where spu_code='SPU-10001');
insert into mall_sku(spu_id,sku_code,sku_name,spec_json,image_url,price,market_price,available_stock,status,del_flag,create_by,create_time)
select spu_id,'SKU-10001-A','原木色','{"颜色":"原木色"}','/assets/chair.jpg',399,459,48,'1','0','system',now() from mall_spu where spu_code='SPU-10001' and not exists(select 1 from mall_sku where sku_code='SKU-10001-A' and del_flag='0');
insert into mall_sku(spu_id,sku_code,sku_name,spec_json,image_url,price,market_price,available_stock,status,del_flag,create_by,create_time)
select spu_id,'SKU-10001-B','胡桃色','{"颜色":"胡桃色"}','/assets/chair.jpg',439,499,0,'1','0','system',now() from mall_spu where spu_code='SPU-10001' and not exists(select 1 from mall_sku where sku_code='SKU-10001-B' and del_flag='0');

-- 若依后台 RBAC 权限。只创建权限定义，不自动授予普通角色；超级管理员默认拥有全部权限。
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3000,'商城运营',0,20,'mall',null,null,'',1,0,'M','0','0',null,'shopping','system',now(),'商城后台权限目录'
where not exists(select 1 from sys_menu where menu_id=3000 or path='mall');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3001,'会员管理',3000,1,'member',null,null,'',1,0,'C','0','0','mall:member:list','user','system',now(),'商城会员管理'
where not exists(select 1 from sys_menu where menu_id=3001 or perms='mall:member:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3002,'分类管理',3000,2,'category',null,null,'',1,0,'C','0','0','mall:category:list','tree-table','system',now(),'商品分类管理'
where not exists(select 1 from sys_menu where menu_id=3002 or perms='mall:category:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3003,'品牌管理',3000,3,'brand',null,null,'',1,0,'C','0','0','mall:brand:list','component','system',now(),'商品品牌管理'
where not exists(select 1 from sys_menu where menu_id=3003 or perms='mall:brand:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3004,'商品管理',3000,4,'product',null,null,'',1,0,'C','0','0','mall:product:list','shopping','system',now(),'SPU 与 SKU 管理'
where not exists(select 1 from sys_menu where menu_id=3004 or perms='mall:product:list');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3010,'会员查询',3001,1,'',null,null,'',1,0,'F','0','0','mall:member:query','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:member:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3011,'会员修改',3001,2,'',null,null,'',1,0,'F','0','0','mall:member:edit','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:member:edit');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3012,'会员删除',3001,3,'',null,null,'',1,0,'F','0','0','mall:member:remove','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:member:remove');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3020,'分类新增',3002,1,'',null,null,'',1,0,'F','0','0','mall:category:add','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:category:add');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3021,'分类修改',3002,2,'',null,null,'',1,0,'F','0','0','mall:category:edit','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:category:edit');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3022,'分类删除',3002,3,'',null,null,'',1,0,'F','0','0','mall:category:remove','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:category:remove');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3030,'品牌新增',3003,1,'',null,null,'',1,0,'F','0','0','mall:brand:add','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:brand:add');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3031,'品牌修改',3003,2,'',null,null,'',1,0,'F','0','0','mall:brand:edit','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:brand:edit');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3032,'品牌删除',3003,3,'',null,null,'',1,0,'F','0','0','mall:brand:remove','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:brand:remove');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3040,'商品查询',3004,1,'',null,null,'',1,0,'F','0','0','mall:product:query','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:product:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3041,'商品新增',3004,2,'',null,null,'',1,0,'F','0','0','mall:product:add','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:product:add');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3042,'商品修改',3004,3,'',null,null,'',1,0,'F','0','0','mall:product:edit','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:product:edit');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3043,'商品上下架',3004,4,'',null,null,'',1,0,'F','0','0','mall:product:publish','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:product:publish');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 3044,'商品删除',3004,5,'',null,null,'',1,0,'F','0','0','mall:product:remove','#','system',now(),'' where not exists(select 1 from sys_menu where perms='mall:product:remove');
