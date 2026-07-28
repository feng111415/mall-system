-- 为商城运营菜单补充 Vue 动态路由组件路径。
-- 旧版本菜单只创建了目录和权限，没有 component，导致点击后内容区域为空白。

update sys_menu set component='mall/member/index', update_by='system', update_time=now()
where menu_id=3001 and (component is null or component='');

update sys_menu set component='mall/category/index', update_by='system', update_time=now()
where menu_id=3002 and (component is null or component='');

update sys_menu set component='mall/brand/index', update_by='system', update_time=now()
where menu_id=3003 and (component is null or component='');

update sys_menu set component='mall/product/index', update_by='system', update_time=now()
where menu_id=3004 and (component is null or component='');

update sys_menu set component='mall/inventory/index', update_by='system', update_time=now()
where menu_id=3050 and (component is null or component='');

update sys_menu set icon='warehouse', update_by='system', update_time=now()
where menu_id=3050 and (icon is null or icon='' or icon='goods');
