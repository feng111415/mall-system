create table if not exists sys_course (
  course_id bigint(20) not null auto_increment comment '课程ID',
  course_name varchar(50) not null comment '课程名称',
  teacher_name varchar(50) not null comment '任课教师',
  credit decimal(4,1) default 0.0 comment '学分',
  status char(1) default '0' comment '状态（0正常 1停用）',
  create_by varchar(64) default '' comment '创建者',
  create_time datetime comment '创建时间',
  update_by varchar(64) default '' comment '更新者',
  update_time datetime comment '更新时间',
  remark varchar(500) default null comment '备注',
  primary key (course_id)
) engine=innodb auto_increment=1 comment='课程管理表';

insert into sys_course (course_name, teacher_name, credit, status, create_by, create_time, remark)
select 'Java程序设计', '张老师', 3.0, '0', 'admin', sysdate(), '示例课程'
where not exists (select 1 from sys_course where course_name = 'Java程序设计');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '课程管理', '1', '9', 'course', 'system/course/index', 1, 0, 'C', '0', '0', 'system:course:list', 'education', 'admin', sysdate(), '', null, '课程管理菜单'
where not exists (select 1 from sys_menu where perms = 'system:course:list');

set @courseMenuId := (select menu_id from sys_menu where perms = 'system:course:list' limit 1);

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '课程管理查询', @courseMenuId, '1', '#', '', 1, 0, 'F', '0', '0', 'system:course:query', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'system:course:query');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '课程管理新增', @courseMenuId, '2', '#', '', 1, 0, 'F', '0', '0', 'system:course:add', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'system:course:add');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '课程管理修改', @courseMenuId, '3', '#', '', 1, 0, 'F', '0', '0', 'system:course:edit', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'system:course:edit');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '课程管理删除', @courseMenuId, '4', '#', '', 1, 0, 'F', '0', '0', 'system:course:remove', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'system:course:remove');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
select '课程管理导出', @courseMenuId, '5', '#', '', 1, 0, 'F', '0', '0', 'system:course:export', '#', 'admin', sysdate(), '', null, ''
where not exists (select 1 from sys_menu where perms = 'system:course:export');

insert into sys_role_menu (role_id, menu_id)
select 1, menu_id from sys_menu where perms like 'system:course:%'
and not exists (select 1 from sys_role_menu rm where rm.role_id = 1 and rm.menu_id = sys_menu.menu_id);
