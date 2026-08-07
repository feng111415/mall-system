-- Mall V0.5 module 5: product review moderation and member submission.
set names utf8mb4;

create table if not exists mall_product_review (
    review_id bigint not null auto_increment,
    order_id bigint not null,
    order_no varchar(64) not null,
    order_item_id bigint not null,
    member_id bigint not null,
    spu_id bigint not null,
    sku_id bigint not null,
    product_name varchar(200) not null,
    sku_name varchar(200),
    product_image varchar(500),
    rating tinyint not null,
    content varchar(1000) not null,
    anonymous char(1) not null default '0',
    status varchar(16) not null default 'PENDING',
    audit_by varchar(64),
    audit_time datetime,
    audit_remark varchar(500),
    create_time datetime not null,
    update_time datetime,
    primary key(review_id),
    unique key uk_mall_product_review_order_item(order_item_id),
    key idx_mall_product_review_spu_status_time(spu_id,status,create_time),
    key idx_mall_product_review_member_time(member_id,create_time),
    key idx_mall_product_review_status_time(status,create_time),
    constraint ck_mall_product_review_rating check(rating between 1 and 5),
    constraint ck_mall_product_review_anonymous check(anonymous in ('0','1')),
    constraint ck_mall_product_review_status check(status in ('PENDING','PUBLISHED','REJECTED'))
) engine=InnoDB default charset=utf8mb4 comment='商城商品评价';

create table if not exists mall_product_review_image (
    review_image_id bigint not null auto_increment,
    review_id bigint not null,
    image_url varchar(500) not null,
    sort_no int not null default 0,
    create_time datetime not null,
    primary key(review_image_id),
    unique key uk_mall_product_review_image(review_id,image_url),
    key idx_mall_product_review_image_review(review_id,sort_no)
) engine=InnoDB default charset=utf8mb4 comment='商城商品评价图片';

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4030,convert(0xe59586e59381e8af84e4bbb7 using utf8mb4),3000,7,'reviews',null,null,'MallProductReviews',1,0,'M','0','0','','comment','mall_v2.23.0',now(),convert(0xe59586e59381e8af84e4bbb7e5aea1e6a0b8 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4030 or (path='reviews' and parent_id=3000));
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4031,convert(0xe8af84e4bbb7e5aea1e6a0b8 using utf8mb4),4030,1,'manage','mall/review/index',null,'MallProductReviewManage',1,0,'C','0','0','mall:review:list','comment','mall_v2.23.0',now(),convert(0xe59586e59381e8af84e4bbb7e58897e8a1a8e5928ce5aea1e6a0b8 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4031 or perms='mall:review:list');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4032,convert(0xe8af84e4bbb7e69fa5e8afa2 using utf8mb4),4031,1,'','',null,'',1,0,'F','0','0','mall:review:query','#','mall_v2.23.0',now(),convert(0xe69fa5e79c8be8af84e4bbb7e8afa6e68385 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4032 or perms='mall:review:query');
insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4033,convert(0xe8af84e4bbb7e5aea1e6a0b8 using utf8mb4),4031,2,'','',null,'',1,0,'F','0','0','mall:review:audit','#','mall_v2.23.0',now(),convert(0xe58f91e5b883e68896e9a9b3e59b9ee8af84e4bbb7 using utf8mb4)
where not exists(select 1 from sys_menu where menu_id=4033 or perms='mall:review:audit');

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key in ('mall_ops_lead','mall_product_ops') and m.menu_id between 4030 and 4033;
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_customer_service' and m.menu_id in (4030,4031,4032);
