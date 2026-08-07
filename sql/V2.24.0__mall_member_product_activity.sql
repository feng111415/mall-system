-- Mall V0.5 module 6: member favorites and browsing history.
set names utf8mb4;

create table if not exists mall_member_favorite (
    favorite_id bigint not null auto_increment,
    member_id bigint not null,
    spu_id bigint not null,
    product_name_snapshot varchar(200) not null,
    subtitle_snapshot varchar(500),
    product_image_snapshot varchar(500),
    price_snapshot decimal(12,2),
    create_time datetime not null,
    primary key(favorite_id),
    unique key uk_mall_member_favorite_member_spu(member_id,spu_id),
    key idx_mall_member_favorite_member_time(member_id,create_time)
) engine=InnoDB default charset=utf8mb4 comment='Mall member favorites';

create table if not exists mall_member_browse_history (
    history_id bigint not null auto_increment,
    member_id bigint not null,
    spu_id bigint not null,
    product_name_snapshot varchar(200) not null,
    subtitle_snapshot varchar(500),
    product_image_snapshot varchar(500),
    price_snapshot decimal(12,2),
    first_view_time datetime not null,
    last_view_time datetime not null,
    view_count int not null default 1,
    primary key(history_id),
    unique key uk_mall_browse_history_member_spu(member_id,spu_id),
    key idx_mall_browse_history_member_time(member_id,last_view_time),
    constraint ck_mall_browse_history_count check(view_count>0)
) engine=InnoDB default charset=utf8mb4 comment='Mall member browsing history';
