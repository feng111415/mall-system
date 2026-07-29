-- Roll back V2.8.0 in an isolated verification database only.
update mall_payment
set failure_reason=case
        when status='REFUNDED' then '版本回滚前已完成异常支付退款'
        else '版本回滚时异常支付退款尚未完成'
    end,
    status='CLOSED'
where status in ('REFUNDING','REFUNDED');

alter table mall_payment drop check ck_mall_payment_status;
alter table mall_payment drop index uk_mall_payment_provider_refund;
alter table mall_payment drop column refund_time, drop column provider_refund_no;
alter table mall_payment add constraint ck_mall_payment_status
    check(status in ('CREATING','PAYING','SUCCESS','FAILED','CLOSED'));
