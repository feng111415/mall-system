set names utf8mb4;

delete rm
from sys_role_menu rm
join sys_role r on r.role_id=rm.role_id
where rm.menu_id=3981
  and r.role_key='mall_finance_risk'
  and r.create_by='mall_v2.13.0';
