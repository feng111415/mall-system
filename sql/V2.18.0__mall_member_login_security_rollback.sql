-- Rollback for V2.18.0. Run only against the migration verification database.
set names utf8mb4;
drop table if exists mall_member_captcha_challenge;
