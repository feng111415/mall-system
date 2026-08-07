-- Rollback V2.23.1. Run only after removing ARRIVED test data from the verification database.
set names utf8mb4;

alter table mall_logistics_node drop check ck_mall_logistics_node_status;
alter table mall_logistics_node
    add constraint ck_mall_logistics_node_status
    check(node_status in ('SHIPPED','IN_TRANSIT','OUT_FOR_DELIVERY','DELIVERED','EXCEPTION','CORRECTION'));
