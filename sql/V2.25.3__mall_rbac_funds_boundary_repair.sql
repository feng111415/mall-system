-- Repair role-menu drift around the after-sale funds center.
set names utf8mb4;

delete rm
from sys_role_menu rm
join sys_role r on r.role_id=rm.role_id
where r.role_key in ('mall_product_ops','mall_fulfillment')
  and rm.menu_id in (4000,4001,4002,4003,4004);
