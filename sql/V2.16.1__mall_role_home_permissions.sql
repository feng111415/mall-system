-- Keep role-home function entries aligned with the real RuoYi menu tree.
set names utf8mb4;

-- Finance and risk already owns the order-center list/query permissions from V2.14.0.
-- Add the missing directory ancestor so RuoYi can generate the authorized route.
insert ignore into sys_role_menu(role_id,menu_id)
select r.role_id,m.menu_id
from sys_role r join sys_menu m on m.menu_id=3981
where r.role_key='mall_finance_risk'
  and r.create_by='mall_v2.13.0'
  and exists (
      select 1 from sys_role_menu child
      where child.role_id=r.role_id and child.menu_id=3991
  );
