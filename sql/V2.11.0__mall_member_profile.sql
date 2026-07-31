-- Mall V0.3 module 4: member profile change audit.
set names utf8mb4;

create table if not exists mall_member_profile_audit (
 audit_id bigint not null auto_increment,
 member_id bigint not null,
 change_type varchar(20) not null,
 old_value varchar(500),
 new_value varchar(500) not null,
 change_source varchar(20) not null,
 request_ip varchar(64),
 create_time datetime not null,
 primary key(audit_id),
 key idx_member_profile_audit_member_type_time(member_id,change_type,create_time),
 constraint ck_member_profile_audit_type check(change_type in ('NICKNAME','AVATAR')),
 constraint ck_member_profile_audit_source check(change_source in ('MEMBER_PORTAL','ADMIN'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员资料修改审计';

alter table mall_member_profile_audit comment = '商城会员资料修改审计';
