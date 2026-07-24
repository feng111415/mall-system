-- ----------------------------
-- 钢琴课后作业助手模块
-- 版本：V1.0.1
-- 变更时间：2026-07-06
-- 变更内容：新增学生、作业、提交点评表，并初始化学生档案后台菜单权限
-- ----------------------------

create table if not exists piano_student (
  student_id bigint(20) not null auto_increment comment '学生ID',
  student_name varchar(50) not null comment '学生姓名',
  parent_name varchar(50) default '' comment '家长姓名',
  parent_phone varchar(20) default '' comment '家长联系电话',
  level_name varchar(50) default '' comment '学习级别',
  learning_goal varchar(200) default '' comment '学习目标',
  status char(1) default '0' comment '状态（0在学 1停用）',
  del_flag char(1) default '0' comment '删除标志（0存在 2删除）',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (student_id),
  key idx_piano_student_name (student_name),
  key idx_piano_student_status (status)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='钢琴学生档案表';

create table if not exists piano_homework (
  homework_id bigint(20) not null auto_increment comment '作业ID',
  student_id bigint(20) not null comment '学生ID',
  homework_title varchar(100) not null comment '作业标题',
  practice_content text not null comment '练习内容',
  practice_requirement varchar(1000) default null comment '练习要求',
  submit_deadline datetime default null comment '提交截止时间',
  homework_status char(1) default '0' comment '作业状态（0待提交 1已提交待点评 2需订正 3已完成 4已逾期）',
  teacher_remark varchar(500) default '' comment '老师备注',
  del_flag char(1) default '0' comment '删除标志（0存在 2删除）',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (homework_id),
  key idx_piano_homework_student (student_id),
  key idx_piano_homework_status (homework_status),
  key idx_piano_homework_deadline (submit_deadline)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='钢琴课后作业表';

create table if not exists piano_homework_submission (
  submission_id bigint(20) not null auto_increment comment '提交ID',
  homework_id bigint(20) not null comment '作业ID',
  student_id bigint(20) not null comment '学生ID',
  attempt_no int(11) not null default 1 comment '第几次提交',
  submit_type char(1) default '0' comment '提交类型（0首次提交 1订正提交）',
  submit_content varchar(1000) default '' comment '学生提交说明',
  attachment_url varchar(500) default '' comment '附件地址',
  submit_time datetime default null comment '提交时间',
  overdue_flag char(1) default '0' comment '是否逾期提交（0否 1是）',
  review_status char(1) default '0' comment '点评状态（0待点评 1已点评）',
  score int(11) default null comment '老师评分',
  review_content varchar(1000) default '' comment '老师点评',
  correction_required char(1) default '0' comment '是否需要订正（0否 1是）',
  review_time datetime default null comment '点评时间',
  latest_flag char(1) default '1' comment '是否最新提交（0否 1是）',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (submission_id),
  unique key uk_piano_submission_attempt (homework_id, attempt_no),
  key idx_piano_submission_homework (homework_id),
  key idx_piano_submission_student (student_id),
  key idx_piano_submission_review_status (review_status),
  key idx_piano_submission_latest (homework_id, latest_flag),
  key idx_piano_submission_overdue (overdue_flag)
) engine=innodb auto_increment=1 default charset=utf8mb4 comment='钢琴作业提交点评表';

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '钢琴作业', '0', '5', 'piano', null, 1, 0, 'M', '0', '0', '', 'education', 'admin', sysdate(), '', null, '钢琴课后作业助手目录'
where not exists (select 1 from sys_menu where menu_name = '钢琴作业' and parent_id = 0 and path = 'piano');

set @pianoMenuId := (select menu_id from sys_menu where menu_name = '钢琴作业' and parent_id = 0 and path = 'piano' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '今日工作台', @pianoMenuId, '0', 'dashboard', 'piano/dashboard/index', 1, 0, 'C', '0', '0', 'piano:dashboard:list', 'dashboard', 'admin', sysdate(), '', null, '钢琴作业今日工作台菜单'
where not exists (select 1 from sys_menu where perms = 'piano:dashboard:list');

set @dashboardMenuId := (select menu_id from sys_menu where perms = 'piano:dashboard:list' limit 1);

insert into sys_role_menu (role_id, menu_id)
select 1, menu_id from sys_menu
where menu_id = @dashboardMenuId
and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = sys_menu.menu_id);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生档案', @pianoMenuId, '1', 'student', 'piano/student/index', 1, 0, 'C', '0', '0', 'piano:student:list', 'peoples', 'admin', sysdate(), '', null, '钢琴学生档案菜单'
where not exists (select 1 from sys_menu where perms = 'piano:student:list');

set @studentMenuId := (select menu_id from sys_menu where perms = 'piano:student:list' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生档案查询', @studentMenuId, '1', '#', '', 1, 0, 'F', '0', '0', 'piano:student:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:student:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生档案新增', @studentMenuId, '2', '#', '', 1, 0, 'F', '0', '0', 'piano:student:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:student:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生档案修改', @studentMenuId, '3', '#', '', 1, 0, 'F', '0', '0', 'piano:student:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:student:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生档案删除', @studentMenuId, '4', '#', '', 1, 0, 'F', '0', '0', 'piano:student:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:student:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '学生档案导出', @studentMenuId, '5', '#', '', 1, 0, 'F', '0', '0', 'piano:student:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:student:export');

insert into sys_role_menu (role_id, menu_id)
select 1, menu_id from sys_menu
where (menu_id = @pianoMenuId or menu_id = @studentMenuId or perms like 'piano:student:%')
and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = sys_menu.menu_id);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '作业管理', @pianoMenuId, '2', 'homework', 'piano/homework/index', 1, 0, 'C', '0', '0', 'piano:homework:list', 'form', 'admin', sysdate(), '', null, '钢琴课后作业菜单'
where not exists (select 1 from sys_menu where perms = 'piano:homework:list');

set @homeworkMenuId := (select menu_id from sys_menu where perms = 'piano:homework:list' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '作业管理查询', @homeworkMenuId, '1', '#', '', 1, 0, 'F', '0', '0', 'piano:homework:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:homework:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '作业管理新增', @homeworkMenuId, '2', '#', '', 1, 0, 'F', '0', '0', 'piano:homework:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:homework:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '作业管理修改', @homeworkMenuId, '3', '#', '', 1, 0, 'F', '0', '0', 'piano:homework:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:homework:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '作业管理删除', @homeworkMenuId, '4', '#', '', 1, 0, 'F', '0', '0', 'piano:homework:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:homework:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '作业管理导出', @homeworkMenuId, '5', '#', '', 1, 0, 'F', '0', '0', 'piano:homework:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:homework:export');

insert into sys_role_menu (role_id, menu_id)
select 1, menu_id from sys_menu
where (menu_id = @homeworkMenuId or perms like 'piano:homework:%')
and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = sys_menu.menu_id);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评', @pianoMenuId, '3', 'submission', 'piano/submission/index', 1, 0, 'C', '0', '0', 'piano:submission:list', 'message', 'admin', sysdate(), '', null, '钢琴作业提交点评菜单'
where not exists (select 1 from sys_menu where perms = 'piano:submission:list');

set @submissionMenuId := (select menu_id from sys_menu where perms = 'piano:submission:list' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评查询', @submissionMenuId, '1', '#', '', 1, 0, 'F', '0', '0', 'piano:submission:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:submission:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评新增', @submissionMenuId, '2', '#', '', 1, 0, 'F', '0', '0', 'piano:submission:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:submission:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评修改', @submissionMenuId, '3', '#', '', 1, 0, 'F', '0', '0', 'piano:submission:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:submission:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评删除', @submissionMenuId, '4', '#', '', 1, 0, 'F', '0', '0', 'piano:submission:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:submission:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评处理', @submissionMenuId, '5', '#', '', 1, 0, 'F', '0', '0', 'piano:submission:review', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:submission:review');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '提交点评导出', @submissionMenuId, '6', '#', '', 1, 0, 'F', '0', '0', 'piano:submission:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'piano:submission:export');

insert into sys_role_menu (role_id, menu_id)
select 1, menu_id from sys_menu
where (menu_id = @submissionMenuId or perms like 'piano:submission:%')
and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = sys_menu.menu_id);
