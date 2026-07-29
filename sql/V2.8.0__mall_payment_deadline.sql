-- Mall V0.3 module 1: fixed payment windows and late-payment refunds.
alter table mall_payment
    add column provider_refund_no varchar(128) null after failure_reason,
    add column refund_time datetime null after paid_time,
    add unique key uk_mall_payment_provider_refund(provider_refund_no);

alter table mall_payment drop check ck_mall_payment_status;
alter table mall_payment add constraint ck_mall_payment_status
    check(status in ('CREATING','PAYING','SUCCESS','FAILED','CLOSED','REFUNDING','REFUNDED'));
