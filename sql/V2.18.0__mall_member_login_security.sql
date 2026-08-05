-- Mall V0.4 module 6: risk-triggered member login challenges.
set names utf8mb4;

create table if not exists mall_member_captcha_challenge (
 challenge_id bigint not null auto_increment,
 challenge_key char(64) not null,
 phone varchar(20) not null,
 device_identifier varchar(64) not null,
 request_ip varchar(64) not null,
 answer_hash char(64) not null,
 ticket_hash char(64),
 status varchar(16) not null,
 verify_attempts int not null default 0,
 expire_time datetime not null,
 ticket_expire_time datetime,
 verified_time datetime,
 consumed_time datetime,
 create_time datetime not null,
 primary key(challenge_id),
 unique key uk_member_captcha_challenge_key(challenge_key),
 key idx_member_captcha_phone_time(phone,create_time),
 key idx_member_captcha_ticket(ticket_hash,status),
 constraint ck_member_captcha_status check(status in ('ISSUED','VERIFIED','CONSUMED','FAILED','EXPIRED'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员风险图形验证挑战';
