-- Mall V0.5 module 1: homepage content operations.
set names utf8mb4;

create table if not exists mall_home_content (
    content_id bigint not null auto_increment,
    content_type varchar(32) not null,
    content_name varchar(120) not null,
    status varchar(16) not null default 'DRAFT',
    effective_time datetime not null,
    expire_time datetime null,
    sort_no int not null default 0,
    hero_eyebrow varchar(80),
    hero_title varchar(200),
    hero_subtitle varchar(500),
    hero_action_text varchar(40),
    hero_target_type varchar(32),
    hero_target_value varchar(120),
    hero_primary_image varchar(500),
    hero_secondary_image varchar(500),
    hero_new_item_count int,
    recommendation_kicker varchar(80),
    recommendation_title varchar(120),
    recommendation_target_type varchar(32),
    recommendation_target_value varchar(120),
    recommendation_display_count int not null default 4,
    recommendation_fallback_mode varchar(32) not null default 'SALES',
    del_flag char(1) not null default '0',
    create_by varchar(64),
    create_time datetime not null,
    update_by varchar(64),
    update_time datetime,
    primary key(content_id),
    key idx_mall_home_content_type_status_time(content_type,status,effective_time,expire_time),
    key idx_mall_home_content_name(content_name),
    constraint ck_mall_home_content_type check(content_type in ('HERO','TICKER','RECOMMENDATION')),
    constraint ck_mall_home_content_status check(status in ('DRAFT','PUBLISHED','DISABLED')),
    constraint ck_mall_home_content_time check(expire_time is null or expire_time > effective_time)
) engine=InnoDB default charset=utf8mb4 comment='商城首页运营内容';

create table if not exists mall_home_content_ticker (
    ticker_id bigint not null auto_increment,
    content_id bigint not null,
    sort_no int not null default 1,
    label varchar(80) not null,
    text varchar(200) not null,
    primary key(ticker_id),
    unique key uk_mall_home_ticker_order(content_id,sort_no),
    key idx_mall_home_ticker_content(content_id)
) engine=InnoDB default charset=utf8mb4 comment='商城首页公告条内容';

create table if not exists mall_home_content_product (
    relation_id bigint not null auto_increment,
    content_id bigint not null,
    spu_id bigint not null,
    sort_no int not null default 1,
    primary key(relation_id),
    unique key uk_mall_home_content_product(content_id,spu_id),
    unique key uk_mall_home_content_product_order(content_id,sort_no),
    key idx_mall_home_content_product_spu(spu_id)
) engine=InnoDB default charset=utf8mb4 comment='商城首页推荐商品';

-- Keep page-content management under the existing mall root. Only operations lead and product operations receive it.
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4010,convert(0xe9a1b5e99da2e58685e5aeb9 using utf8mb4),3000,5,'content',null,null,'MallContent',1,0,'M','0','0','','edit','mall_v2.19.0',now(),convert(0xe9a696e9a1b5e58685e5aeb9e8bf90e890a5 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4010 or path='content' and parent_id=3000);

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4011,convert(0xe9a696e9a1b5e58685e5aeb9 using utf8mb4),4010,1,'homepage','mall/content/index',null,'MallHomepageContent',1,0,'C','0','0','mall:content:list','homepage','mall_v2.19.0',now(),convert(0xe7bc96e8be91e9a696e9a1b5e58685e5aeb9e4b8bbe8a681e58cbae59f9fe38081e5ae9ae697b6e58f91e5b883e4b88ee4bc98e58c96e68e92e5ba8f using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4011 or perms='mall:content:list');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4012,convert(0xe69fa5e8afa2 using utf8mb4),4011,1,'','',null,'',1,0,'F','0','0','mall:content:query','#','mall_v2.19.0',now(),convert(0xe69fa5e696b0e58685e5aeb9e8afa6e68385 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4012 or perms='mall:content:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4013,convert(0xe696b0e5a29e using utf8mb4),4011,2,'','',null,'',1,0,'F','0','0','mall:content:add','#','mall_v2.19.0',now(),convert(0xe696b0e5a29ee9a696e9a1b5e58685e5aeb9 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4013 or perms='mall:content:add');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4014,convert(0xe7bc96e8be91 using utf8mb4),4011,3,'','',null,'',1,0,'F','0','0','mall:content:edit','#','mall_v2.19.0',now(),convert(0xe7bc96e8be91e9a696e9a1b5e58685e5aeb9 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4014 or perms='mall:content:edit');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4015,convert(0xe58f91e5b883 using utf8mb4),4011,4,'','',null,'',1,0,'F','0','0','mall:content:publish','#','mall_v2.19.0',now(),convert(0xe5ae9ae697b6e58f91e5b883e9a696e9a1b5e58685e5aeb9 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4015 or perms='mall:content:publish');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4016,convert(0xe5819ce794a8 using utf8mb4),4011,5,'','',null,'',1,0,'F','0','0','mall:content:disable','#','mall_v2.19.0',now(),convert(0xe5819ce794a8e9a696e9a1b5e58685e5aeb9 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4016 or perms='mall:content:disable');

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.create_by='mall_v2.13.0' and r.role_key in ('mall_ops_lead','mall_product_ops')
  and m.menu_id in (4010,4011,4012,4013,4014,4015,4016);
