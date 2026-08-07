-- Mall V0.5 module 3: member account and privacy lifecycle.
set names utf8mb4;

alter table mall_member
    add column deactivated_time datetime null comment 'Account deactivation time' after status;

create table if not exists mall_member_phone_change_request (
    request_id bigint not null auto_increment,
    member_id bigint not null,
    old_phone varchar(20) not null,
    new_phone varchar(20) not null,
    status varchar(24) not null default 'OLD_PHONE_PENDING',
    old_verified_time datetime null,
    new_verified_time datetime null,
    expires_time datetime not null,
    completed_time datetime null,
    request_ip varchar(64) not null,
    create_time datetime not null,
    update_time datetime not null,
    primary key(request_id),
    key idx_member_phone_change_member_status(member_id,status,create_time),
    key idx_member_phone_change_new_phone(new_phone,status),
    constraint ck_member_phone_change_status check(status in ('OLD_PHONE_PENDING','NEW_PHONE_PENDING','COMPLETED','CANCELLED','EXPIRED'))
) engine=InnoDB default charset=utf8mb4 comment='Mall member phone change request';

create table if not exists mall_member_account_action_ticket (
    ticket_id bigint not null auto_increment,
    request_id bigint null,
    member_id bigint not null,
    action_type varchar(32) not null,
    ticket_hash char(64) not null,
    expire_time datetime not null,
    consumed_time datetime null,
    create_time datetime not null,
    primary key(ticket_id),
    unique key uk_member_account_ticket_hash(ticket_hash),
    key idx_member_account_ticket_member_action(member_id,action_type,create_time),
    constraint ck_member_account_ticket_action check(action_type in ('PHONE_CHANGE_OLD_VERIFIED','ACCOUNT_CANCEL_VERIFIED'))
) engine=InnoDB default charset=utf8mb4 comment='Mall member account action ticket';

create table if not exists mall_member_lifecycle_audit (
    audit_id bigint not null auto_increment,
    member_id bigint not null,
    event_type varchar(40) not null,
    title varchar(120) not null,
    summary varchar(500),
    old_phone varchar(20),
    new_phone varchar(20),
    request_ip varchar(64),
    create_time datetime not null,
    primary key(audit_id),
    key idx_member_lifecycle_member_time(member_id,create_time),
    constraint ck_member_lifecycle_event check(event_type in ('PHONE_CHANGE_STARTED','PHONE_CHANGE_OLD_VERIFIED','PHONE_CHANGED','ACCOUNT_CANCELLATION_BLOCKED','ACCOUNT_DEACTIVATED'))
) engine=InnoDB default charset=utf8mb4 comment='Mall member lifecycle audit';
