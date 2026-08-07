-- Rollback for V2.21.0. Run only against the migration verification database.
set names utf8mb4;
drop table if exists mall_member_lifecycle_audit;
drop table if exists mall_member_account_action_ticket;
drop table if exists mall_member_phone_change_request;
alter table mall_member drop column deactivated_time;
