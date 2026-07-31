-- Mall V0.3 module 5: member devices, sessions and primary-mobile audit.
set names utf8mb4;

create table if not exists mall_member_device (
 member_device_id bigint not null auto_increment,
 member_id bigint not null,
 device_identifier char(32) not null,
 device_type varchar(16) not null,
 device_name varchar(100) not null,
 primary_mobile char(1) not null default '0',
 first_seen_time datetime not null,
 last_login_time datetime not null,
 last_login_ip varchar(64),
 create_time datetime not null,
 update_time datetime not null,
 primary key(member_device_id),
 unique key uk_member_device_identifier(member_id,device_identifier),
 key idx_member_device_primary(member_id,device_type,primary_mobile),
 constraint ck_member_device_type check(device_type in ('MOBILE','DESKTOP')),
 constraint ck_member_device_primary check(primary_mobile in ('0','1'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员浏览器设备';

create table if not exists mall_member_session (
 session_id bigint not null auto_increment,
 member_id bigint not null,
 member_device_id bigint not null,
 token_hash char(64) not null,
 status varchar(16) not null,
 login_ip varchar(64),
 last_active_time datetime not null,
 expire_time datetime not null,
 offline_reason varchar(64),
 create_time datetime not null,
 update_time datetime not null,
 primary key(session_id),
 unique key uk_member_session_token_hash(token_hash),
 key idx_member_session_member_status(member_id,status,expire_time),
 key idx_member_session_device_status(member_device_id,status),
 constraint ck_member_session_status check(status in ('ACTIVE','REPLACED','LOGGED_OUT','REVOKED','EXPIRED'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员在线会话';

create table if not exists mall_member_device_audit (
 audit_id bigint not null auto_increment,
 member_id bigint not null,
 action_type varchar(32) not null,
 old_member_device_id bigint,
 new_member_device_id bigint not null,
 request_ip varchar(64),
 create_time datetime not null,
 primary key(audit_id),
 key idx_member_device_audit_member_action_time(member_id,action_type,create_time),
 constraint ck_member_device_audit_action check(action_type in ('PRIMARY_MOBILE_REPLACED'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员主设备变更审计';

alter table mall_member_device comment = '商城会员浏览器设备';
alter table mall_member_session comment = '商城会员在线会话';
alter table mall_member_device_audit comment = '商城会员主设备变更审计';
