-- Mall V0.2: storefront catalog fixtures for the dedicated migration test database.
-- The inserts are keyed by stable business codes so re-running the script is safe.

insert into mall_category(parent_id, category_name, level, category_path, sort_no, status, del_flag, create_by, create_time)
select 0, '出行', 1, null, 50, '0', '0', 'system', now()
where not exists (select 1 from mall_category where category_name = '出行' and del_flag = '0');

update mall_category
set category_path = concat('/', category_id, '/')
where category_name = '出行' and del_flag = '0';

insert into mall_brand(brand_name, description, sort_no, status, del_flag, create_by, create_time)
select '光线研究所', '为日常空间设计耐用的小型器物', 20, '0', '0', 'system', now()
where not exists (select 1 from mall_brand where brand_name = '光线研究所');
insert into mall_brand(brand_name, description, sort_no, status, del_flag, create_by, create_time)
select '行摄记录', '轻量可靠的影像与音频设备', 30, '0', '0', 'system', now()
where not exists (select 1 from mall_brand where brand_name = '行摄记录');
insert into mall_brand(brand_name, description, sort_no, status, del_flag, create_by, create_time)
select '城市步履', '适合通勤与周末出发的随身装备', 40, '0', '0', 'system', now()
where not exists (select 1 from mall_brand where brand_name = '城市步履');
insert into mall_brand(brand_name, description, sort_no, status, del_flag, create_by, create_time)
select '山口咖啡', '为慢一点的早晨准备的咖啡器具', 50, '0', '0', 'system', now()
where not exists (select 1 from mall_brand where brand_name = '山口咖啡');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10002', '暖光阅读台灯', '无极调光，给夜晚留一束舒服的光', '/assets/lamp.jpg',
    '<p>暖光阅读台灯采用金属灯臂与低眩光灯罩，适合书桌和床边使用。</p>', 269, 299, '1', 20, 128, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '光线研究所'
where c.category_name = '家居' and not exists (select 1 from mall_spu where spu_code = 'SPU-10002');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10003', '便携无反相机', '轻装出发，也能认真记录眼前的一切', '/assets/camera.jpg',
    '<p>便携无反相机配备常用焦段镜头，适合旅行和日常记录。</p>', 3499, 3799, '1', 30, 64, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '行摄记录'
where c.category_name = '数码' and not exists (select 1 from mall_spu where spu_code = 'SPU-10003');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10004', '静音头戴耳机', '安静下来，听见更完整的细节', '/assets/headphones.jpg',
    '<p>封闭式耳罩与舒适头梁设计，适合通勤和专注工作时使用。</p>', 699, 799, '1', 40, 203, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '行摄记录'
where c.category_name = '数码' and not exists (select 1 from mall_spu where spu_code = 'SPU-10004');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10005', '极简复古腕表', '把时间留在干净利落的表盘上', '/assets/watch.jpg',
    '<p>简约表盘搭配真皮表带，适合日常通勤与正式场合。</p>', 528, 568, '1', 50, 176, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '城市步履'
where c.category_name = '穿搭' and not exists (select 1 from mall_spu where spu_code = 'SPU-10005');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10006', '轻量城市跑鞋', '为每天多走一点路做好准备', '/assets/sneaker.jpg',
    '<p>轻量缓震中底与透气鞋面，适合城市步行和轻度运动。</p>', 299, 329, '1', 60, 241, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '城市步履'
where c.category_name = '穿搭' and not exists (select 1 from mall_spu where spu_code = 'SPU-10006');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10007', '手冲咖啡套装', '一套顺手的器具，慢慢开始一天', '/assets/coffee.jpg',
    '<p>包含分享壶、滤杯和量勺，满足两人以内的手冲咖啡需求。</p>', 188, 228, '1', 70, 112, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '山口咖啡'
where c.category_name = '咖啡' and not exists (select 1 from mall_spu where spu_code = 'SPU-10007');

insert into mall_spu(category_id, brand_id, spu_code, product_name, subtitle, main_image, detail_html,
    price_min, price_max, publish_status, sort_no, sales_count, del_flag, create_by, create_time)
select c.category_id, b.brand_id, 'SPU-10008', '城市通勤双肩包', '从工作日到短途出发，都装得下', '/assets/backpack.jpg',
    '<p>分区收纳设计，内置电脑夹层与侧边水杯位。</p>', 359, 399, '1', 80, 95, '0', 'system', now()
from mall_category c join mall_brand b on b.brand_name = '城市步履'
where c.category_name = '出行' and not exists (select 1 from mall_spu where spu_code = 'SPU-10008');

insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10002-A', '砂岩白', '{"颜色":"砂岩白"}', '/assets/lamp.jpg', 269, 329, 36, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10002' and not exists (select 1 from mall_sku where sku_code = 'SKU-10002-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10002-B', '墨灰色', '{"颜色":"墨灰色"}', '/assets/lamp.jpg', 299, 359, 8, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10002' and not exists (select 1 from mall_sku where sku_code = 'SKU-10002-B' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10003-A', '曜石黑', '{"颜色":"曜石黑"}', '/assets/camera.jpg', 3499, 3999, 5, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10003' and not exists (select 1 from mall_sku where sku_code = 'SKU-10003-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10003-B', '银灰色', '{"颜色":"银灰色"}', '/assets/camera.jpg', 3799, 4299, 2, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10003' and not exists (select 1 from mall_sku where sku_code = 'SKU-10003-B' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10004-A', '深空灰', '{"颜色":"深空灰"}', '/assets/headphones.jpg', 699, 899, 0, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10004' and not exists (select 1 from mall_sku where sku_code = 'SKU-10004-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10004-B', '奶油白', '{"颜色":"奶油白"}', '/assets/headphones.jpg', 799, 999, 14, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10004' and not exists (select 1 from mall_sku where sku_code = 'SKU-10004-B' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10005-A', '黑色皮表带', '{"表带":"黑色皮表带"}', '/assets/watch.jpg', 528, 699, 18, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10005' and not exists (select 1 from mall_sku where sku_code = 'SKU-10005-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10005-B', '棕色皮表带', '{"表带":"棕色皮表带"}', '/assets/watch.jpg', 568, 739, 6, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10005' and not exists (select 1 from mall_sku where sku_code = 'SKU-10005-B' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10006-A', '云雾白 38', '{"颜色":"云雾白","尺码":"38"}', '/assets/sneaker.jpg', 299, 399, 16, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10006' and not exists (select 1 from mall_sku where sku_code = 'SKU-10006-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10006-B', '云雾白 40', '{"颜色":"云雾白","尺码":"40"}', '/assets/sneaker.jpg', 299, 399, 3, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10006' and not exists (select 1 from mall_sku where sku_code = 'SKU-10006-B' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10006-C', '夜岩灰 42', '{"颜色":"夜岩灰","尺码":"42"}', '/assets/sneaker.jpg', 329, 429, 10, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10006' and not exists (select 1 from mall_sku where sku_code = 'SKU-10006-C' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10007-A', '透明分享壶', '{"版本":"透明分享壶"}', '/assets/coffee.jpg', 188, 239, 21, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10007' and not exists (select 1 from mall_sku where sku_code = 'SKU-10007-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10007-B', '琥珀分享壶', '{"版本":"琥珀分享壶"}', '/assets/coffee.jpg', 228, 279, 9, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10007' and not exists (select 1 from mall_sku where sku_code = 'SKU-10007-B' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10008-A', '岩灰色 16L', '{"颜色":"岩灰色","容量":"16L"}', '/assets/backpack.jpg', 359, 459, 15, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10008' and not exists (select 1 from mall_sku where sku_code = 'SKU-10008-A' and del_flag = '0');
insert into mall_sku(spu_id, sku_code, sku_name, spec_json, image_url, price, market_price, available_stock, status, del_flag, create_by, create_time)
select spu_id, 'SKU-10008-B', '苔绿 20L', '{"颜色":"苔绿","容量":"20L"}', '/assets/backpack.jpg', 399, 499, 7, '1', '0', 'system', now() from mall_spu where spu_code = 'SPU-10008' and not exists (select 1 from mall_sku where sku_code = 'SKU-10008-B' and del_flag = '0');

insert into mall_product_media(spu_id, media_type, media_url, sort_no)
select s.spu_id, 'IMAGE', s.main_image, 10 from mall_spu s
where s.spu_code between 'SPU-10001' and 'SPU-10008'
  and not exists (select 1 from mall_product_media m where m.spu_id = s.spu_id and m.media_url = s.main_image);

insert into mall_stock(sku_id, available_quantity, locked_quantity, sold_quantity, warning_threshold, create_time, update_time)
select sku.sku_id, sku.available_stock, 0, 0, 3, now(), now()
from mall_sku sku
left join mall_stock stock on stock.sku_id = sku.sku_id
where sku.sku_code between 'SKU-10002-A' and 'SKU-10008-B'
  and sku.del_flag = '0' and stock.sku_id is null;
