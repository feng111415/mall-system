-- Localize the V2.22.0 coupon operation menus for the RuoYi sidebar.
set names utf8mb4;

update sys_menu
set menu_name=convert(0xe4bc98e683a0e588b8e8bf90e890a5 using utf8mb4),
    remark=convert(0xe4bc98e683a0e588b8e9858de7bdaee4b88ee58f91e694be using utf8mb4)
where menu_id=4020 and path='coupon';

update sys_menu
set menu_name=convert(0xe4bc98e683a0e588b8e7aea1e79086 using utf8mb4),
    remark=convert(0xe4bc98e683a0e588b8e997ade78eafe8bf90e890a5 using utf8mb4)
where menu_id=4021 and perms='mall:coupon:list';

update sys_menu
set menu_name=convert(0xe69fa5e8afa2 using utf8mb4),
    remark=convert(0xe69fa5e8afa2e4bc98e683a0e588b8e4b88ee9a286e58f96e8aeb0e5bd95 using utf8mb4)
where menu_id=4022 and perms='mall:coupon:query';

update sys_menu
set menu_name=convert(0xe696b0e5a29e using utf8mb4),
    remark=convert(0xe696b0e5bbbae4bc98e683a0e588b8e88d89e7a8bf using utf8mb4)
where menu_id=4023 and perms='mall:coupon:add';

update sys_menu
set menu_name=convert(0xe4bfaee694b9 using utf8mb4),
    remark=convert(0xe4bfaee694b9e4bc98e683a0e588b8e88d89e7a8bf using utf8mb4)
where menu_id=4024 and perms='mall:coupon:edit';

update sys_menu
set menu_name=convert(0xe58f91e5b883e68896e69a82e5819c using utf8mb4),
    remark=convert(0xe58f91e5b883e68896e69a82e5819ce4bc98e683a0e588b8 using utf8mb4)
where menu_id=4025 and perms='mall:coupon:publish';

update sys_menu
set menu_name=convert(0xe6898be5b7a5e58f91e694be using utf8mb4),
    remark=convert(0xe59091e4bc9ae59198e58f91e694bee4bc98e683a0e588b8 using utf8mb4)
where menu_id=4026 and perms='mall:coupon:issue';
