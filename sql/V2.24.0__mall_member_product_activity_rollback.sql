-- Rollback V2.24.0. Run only against the migration verification database.
set names utf8mb4;
drop table if exists mall_member_browse_history;
drop table if exists mall_member_favorite;
