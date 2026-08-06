-- Mall V0.5 module 2: member message center.
set names utf8mb4;

create table if not exists mall_member_message (
    message_id bigint not null auto_increment,
    member_id bigint not null,
    category varchar(20) not null,
    title varchar(120) not null,
    summary varchar(255),
    content varchar(2000),
    business_type varchar(40),
    business_id bigint,
    business_no varchar(80),
    action_path varchar(200),
    read_flag char(1) not null default '0',
    read_time datetime,
    create_time datetime not null,
    expire_time datetime,
    del_flag char(1) not null default '0',
    primary key(message_id),
    key idx_mall_member_message_member_category_time(member_id,category,create_time),
    key idx_mall_member_message_member_read(member_id,read_flag,create_time),
    constraint ck_mall_member_message_category check(category in ('ORDER','LOGISTICS','AFTER_SALE','ACCOUNT')),
    constraint ck_mall_member_message_read check(read_flag in ('0','1'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员业务消息';
