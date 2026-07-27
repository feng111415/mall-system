-- Mall phase three: member-scoped persistent cart. Cart rows never reserve stock.
create table if not exists mall_cart_item (
 cart_item_id bigint not null auto_increment, member_id bigint not null, sku_id bigint not null,
 quantity int not null default 1, selected_flag char(1) not null default '1',
 create_time datetime not null, update_time datetime not null,
 primary key(cart_item_id), unique key uk_mall_cart_member_sku(member_id,sku_id),
 key idx_mall_cart_member(member_id,update_time),
 constraint ck_mall_cart_quantity check(quantity>0),
 constraint ck_mall_cart_selected check(selected_flag in ('0','1'))
) engine=InnoDB default charset=utf8mb4 comment='商城会员购物车';
