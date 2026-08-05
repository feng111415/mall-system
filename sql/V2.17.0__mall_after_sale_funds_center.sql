-- Mall V0.4 module 5: after-sale funds and exception operations center.
set names utf8mb4;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4000,'售后资金处理中心',3982,1,'center','mall/after-sale-funds/index',null,'MallAfterSaleFundsCenter',1,0,'C','0','0','mall:after-sale-funds:center:list','money','mall_v0.4.5',now(),'售后审核、退款、对账告警与补偿任务统一入口'
where not exists(select 1 from sys_menu where menu_id=4000 or perms='mall:after-sale-funds:center:list');
update sys_menu set menu_name='售后资金处理中心',parent_id=3982,order_num=1,path='center',component='mall/after-sale-funds/index',route_name='MallAfterSaleFundsCenter',menu_type='C',visible='0',status='0',perms='mall:after-sale-funds:center:list',icon='money' where menu_id=4000;

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4001,'售后资金中心查询',4000,1,'','','','',1,0,'F','0','0','mall:after-sale-funds:center:query','#','mall_v0.4.5',now(),'按岗位聚合售后和资金异常工作项'
where not exists(select 1 from sys_menu where menu_id=4001 or perms='mall:after-sale-funds:center:query');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4002,'补偿任务查询',4000,2,'','','','',1,0,'F','0','0','mall:compensation:list','#','mall_v0.4.5',now(),'查看支付、退款和库存补偿任务'
where not exists(select 1 from sys_menu where menu_id=4002 or perms='mall:compensation:list');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4003,'补偿任务重试',4000,3,'','','','',1,0,'F','0','0','mall:compensation:retry','#','mall_v0.4.5',now(),'重置失败或人工补偿任务并等待重新执行'
where not exists(select 1 from sys_menu where menu_id=4003 or perms='mall:compensation:retry');

insert into sys_menu(menu_id,menu_name,parent_id,order_num,path,component,query,route_name,is_frame,is_cache,menu_type,visible,status,perms,icon,create_by,create_time,remark)
select 4004,'手动执行补偿',4000,4,'','','','',1,0,'F','0','0','mall:compensation:run','#','mall_v0.4.5',now(),'手动触发到期补偿任务，仅运营主管使用'
where not exists(select 1 from sys_menu where menu_id=4004 or perms='mall:compensation:run');

update sys_menu set order_num=2 where menu_id=3970 and parent_id=3982;
update sys_menu set order_num=3 where menu_id=3900 and parent_id=3982;

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_ops_lead' and r.create_by='mall_v2.13.0' and m.menu_id in (4000,4001,4002,4003,4004);

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_customer_service' and r.create_by='mall_v2.13.0' and m.menu_id in (4000,4001);

insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id from sys_role r join sys_menu m
where r.role_key='mall_finance_risk' and r.create_by='mall_v2.13.0' and m.menu_id in (4000,4001,4002,4003);
