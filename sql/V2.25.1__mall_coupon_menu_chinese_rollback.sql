-- Restore the original V2.22.0 English coupon menu labels.
set names utf8mb4;

update sys_menu set menu_name='Coupon Operations',remark='Coupon configuration and issuance'
where menu_id=4020 and path='coupon';

update sys_menu set menu_name='Coupon Management',remark='Coupon closed loop operations'
where menu_id=4021 and perms='mall:coupon:list';

update sys_menu set menu_name='Query',remark='Query coupons and claims'
where menu_id=4022 and perms='mall:coupon:query';

update sys_menu set menu_name='Create',remark='Create coupon draft'
where menu_id=4023 and perms='mall:coupon:add';

update sys_menu set menu_name='Edit',remark='Edit coupon draft'
where menu_id=4024 and perms='mall:coupon:edit';

update sys_menu set menu_name='Publish or Pause',remark='Publish or pause coupon'
where menu_id=4025 and perms='mall:coupon:publish';

update sys_menu set menu_name='Manual Issue',remark='Issue coupon to member'
where menu_id=4026 and perms='mall:coupon:issue';
