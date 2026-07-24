-- ----------------------------
-- 钢琴作业学生端提交入口
-- 版本：V1.0.2
-- 变更时间：2026-07-06
-- 变更内容：给作业增加提交码，用于小程序端匿名查看作业和提交练习记录
-- ----------------------------

set @column_exists := (
  select count(1)
  from information_schema.columns
  where table_schema = database()
    and table_name = 'piano_homework'
    and column_name = 'submit_code'
);

set @ddl := if(
  @column_exists = 0,
  'alter table piano_homework add column submit_code varchar(16) not null default '''' comment ''学生端提交码'' after homework_id',
  'select 1'
);
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;

update piano_homework
set submit_code = concat('P', upper(substr(replace(uuid(), '-', ''), 1, 9)))
where submit_code is null or submit_code = '';

set @index_exists := (
  select count(1)
  from information_schema.statistics
  where table_schema = database()
    and table_name = 'piano_homework'
    and index_name = 'uk_piano_homework_submit_code'
);

set @ddl := if(
  @index_exists = 0,
  'alter table piano_homework add unique key uk_piano_homework_submit_code (submit_code)',
  'select 1'
);
prepare stmt from @ddl;
execute stmt;
deallocate prepare stmt;
