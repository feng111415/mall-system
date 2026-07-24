-- ----------------------------
-- 抢票小程序业务表
-- 适用数据库：MySQL 8.x
-- 说明：本脚本只包含自有票务/预约业务表，不包含若依系统表。
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ticket_refund_log;
DROP TABLE IF EXISTS ticket_pay_log;
DROP TABLE IF EXISTS ticket_checkin_code;
DROP TABLE IF EXISTS ticket_order_item;
DROP TABLE IF EXISTS ticket_order;
DROP TABLE IF EXISTS ticket_stock_log;
DROP TABLE IF EXISTS ticket_stock;
DROP TABLE IF EXISTS ticket_type;
DROP TABLE IF EXISTS ticket_session;
DROP TABLE IF EXISTS ticket_activity;
DROP TABLE IF EXISTS ticket_passenger;
DROP TABLE IF EXISTS ticket_user;
DROP TABLE IF EXISTS ticket_notify_subscribe;
DROP TABLE IF EXISTS ticket_config;

-- ----------------------------
-- 1、票务配置表
-- ----------------------------
CREATE TABLE ticket_config (
  config_id        bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  config_key       varchar(100)    NOT NULL COMMENT '配置键',
  config_value     varchar(500)    DEFAULT '' COMMENT '配置值',
  config_name      varchar(100)    DEFAULT '' COMMENT '配置名称',
  config_type      char(1)         DEFAULT '0' COMMENT '配置类型（0业务 1系统）',
  status           char(1)         DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by        varchar(64)     DEFAULT '' COMMENT '创建者',
  create_time      datetime        DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)     DEFAULT '' COMMENT '更新者',
  update_time      datetime        DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)    DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (config_id),
  UNIQUE KEY uk_ticket_config_key (config_key)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务配置表';

-- ----------------------------
-- 2、小程序用户表
-- ----------------------------
CREATE TABLE ticket_user (
  user_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '小程序用户ID',
  openid           varchar(64)      NOT NULL COMMENT '微信openid',
  unionid          varchar(64)      DEFAULT '' COMMENT '微信unionid',
  session_key      varchar(128)     DEFAULT '' COMMENT '微信session_key',
  nick_name        varchar(64)      DEFAULT '' COMMENT '昵称',
  avatar           varchar(500)     DEFAULT '' COMMENT '头像地址',
  mobile           varchar(20)      DEFAULT '' COMMENT '手机号',
  real_name_status char(1)          DEFAULT '0' COMMENT '实名状态（0未实名 1已实名）',
  status           char(1)          DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  del_flag         char(1)          DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  last_login_ip    varchar(128)     DEFAULT '' COMMENT '最后登录IP',
  last_login_time  datetime         DEFAULT NULL COMMENT '最后登录时间',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_ticket_user_openid (openid),
  KEY idx_ticket_user_unionid (unionid),
  KEY idx_ticket_user_mobile (mobile)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='小程序用户表';

-- ----------------------------
-- 3、实名购票人表
-- ----------------------------
CREATE TABLE ticket_passenger (
  passenger_id     bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '购票人ID',
  user_id          bigint(20)      NOT NULL COMMENT '小程序用户ID',
  real_name        varchar(50)      NOT NULL COMMENT '真实姓名',
  id_type          char(1)          DEFAULT '0' COMMENT '证件类型（0身份证 1护照 2其他）',
  id_no            varchar(64)      NOT NULL COMMENT '证件号码',
  mobile           varchar(20)      DEFAULT '' COMMENT '手机号',
  passenger_type   char(1)          DEFAULT '0' COMMENT '购票人类型（0成人 1儿童 2学生）',
  is_default       char(1)          DEFAULT '0' COMMENT '是否默认（0否 1是）',
  status           char(1)          DEFAULT '0' COMMENT '状态（0正常 1停用）',
  del_flag         char(1)          DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (passenger_id),
  UNIQUE KEY uk_ticket_passenger_user_id_no (user_id, id_type, id_no),
  KEY idx_ticket_passenger_user (user_id),
  KEY idx_ticket_passenger_mobile (mobile)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='实名购票人表';

-- ----------------------------
-- 4、活动表
-- ----------------------------
CREATE TABLE ticket_activity (
  activity_id      bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  activity_name    varchar(100)     NOT NULL COMMENT '活动名称',
  activity_code    varchar(64)      NOT NULL COMMENT '活动编码',
  activity_type    char(1)          DEFAULT '0' COMMENT '活动类型（0演出 1景区 2赛事 3课程 9其他）',
  cover_url        varchar(500)     DEFAULT '' COMMENT '封面图',
  banner_urls      varchar(2000)    DEFAULT '' COMMENT '轮播图，逗号分隔',
  venue_name       varchar(100)     DEFAULT '' COMMENT '场馆名称',
  venue_address    varchar(255)     DEFAULT '' COMMENT '场馆地址',
  longitude        decimal(10,6)    DEFAULT NULL COMMENT '经度',
  latitude         decimal(10,6)    DEFAULT NULL COMMENT '纬度',
  sale_start_time  datetime         DEFAULT NULL COMMENT '开售时间',
  sale_end_time    datetime         DEFAULT NULL COMMENT '停售时间',
  start_time       datetime         DEFAULT NULL COMMENT '活动开始时间',
  end_time         datetime         DEFAULT NULL COMMENT '活动结束时间',
  purchase_limit   int(11)          DEFAULT 1 COMMENT '每人限购数量',
  need_real_name   char(1)          DEFAULT '1' COMMENT '是否实名（0否 1是）',
  need_seat        char(1)          DEFAULT '0' COMMENT '是否选座（0否 1是）',
  order_timeout    int(11)          DEFAULT 900 COMMENT '订单支付超时秒数',
  status           char(1)          DEFAULT '0' COMMENT '状态（0待上架 1已上架 2已下架 3已结束）',
  del_flag         char(1)          DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  description      text             COMMENT '活动详情',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (activity_id),
  UNIQUE KEY uk_ticket_activity_code (activity_code),
  KEY idx_ticket_activity_status_sale (status, sale_start_time, sale_end_time),
  KEY idx_ticket_activity_time (start_time, end_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='活动表';

-- ----------------------------
-- 5、场次表
-- ----------------------------
CREATE TABLE ticket_session (
  session_id       bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '场次ID',
  activity_id      bigint(20)      NOT NULL COMMENT '活动ID',
  session_name     varchar(100)     NOT NULL COMMENT '场次名称',
  session_code     varchar(64)      NOT NULL COMMENT '场次编码',
  session_start    datetime         NOT NULL COMMENT '场次开始时间',
  session_end      datetime         DEFAULT NULL COMMENT '场次结束时间',
  sale_start_time  datetime         DEFAULT NULL COMMENT '场次开售时间',
  sale_end_time    datetime         DEFAULT NULL COMMENT '场次停售时间',
  venue_name       varchar(100)     DEFAULT '' COMMENT '场馆名称',
  total_stock      int(11)          DEFAULT 0 COMMENT '总库存',
  sold_stock       int(11)          DEFAULT 0 COMMENT '已售库存',
  locked_stock     int(11)          DEFAULT 0 COMMENT '锁定库存',
  status           char(1)          DEFAULT '0' COMMENT '状态（0待售 1销售中 2售罄 3停止销售 4已结束）',
  sort             int(11)          DEFAULT 0 COMMENT '排序',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (session_id),
  UNIQUE KEY uk_ticket_session_code (session_code),
  KEY idx_ticket_session_activity (activity_id),
  KEY idx_ticket_session_sale (status, sale_start_time, sale_end_time),
  KEY idx_ticket_session_start (session_start)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='活动场次表';

-- ----------------------------
-- 6、票种/票档表
-- ----------------------------
CREATE TABLE ticket_type (
  type_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '票种ID',
  activity_id      bigint(20)      NOT NULL COMMENT '活动ID',
  session_id       bigint(20)      NOT NULL COMMENT '场次ID',
  type_name        varchar(100)     NOT NULL COMMENT '票种名称',
  type_code        varchar(64)      NOT NULL COMMENT '票种编码',
  price            decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '票价',
  market_price     decimal(10,2)    DEFAULT 0.00 COMMENT '市场价',
  total_stock      int(11)          DEFAULT 0 COMMENT '总库存',
  sold_stock       int(11)          DEFAULT 0 COMMENT '已售库存',
  locked_stock     int(11)          DEFAULT 0 COMMENT '锁定库存',
  min_buy          int(11)          DEFAULT 1 COMMENT '最小购买数量',
  max_buy          int(11)          DEFAULT 1 COMMENT '最大购买数量',
  status           char(1)          DEFAULT '0' COMMENT '状态（0正常 1停用 2售罄）',
  sort             int(11)          DEFAULT 0 COMMENT '排序',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (type_id),
  UNIQUE KEY uk_ticket_type_code (type_code),
  KEY idx_ticket_type_session (session_id),
  KEY idx_ticket_type_activity (activity_id),
  KEY idx_ticket_type_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票种票档表';

-- ----------------------------
-- 7、库存表
-- ----------------------------
CREATE TABLE ticket_stock (
  stock_id         bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  activity_id      bigint(20)      NOT NULL COMMENT '活动ID',
  session_id       bigint(20)      NOT NULL COMMENT '场次ID',
  type_id          bigint(20)      NOT NULL COMMENT '票种ID',
  total_stock      int(11)          NOT NULL DEFAULT 0 COMMENT '总库存',
  available_stock  int(11)          NOT NULL DEFAULT 0 COMMENT '可售库存',
  locked_stock     int(11)          NOT NULL DEFAULT 0 COMMENT '锁定库存',
  sold_stock       int(11)          NOT NULL DEFAULT 0 COMMENT '已售库存',
  version          int(11)          NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  status           char(1)          DEFAULT '0' COMMENT '状态（0正常 1停用）',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (stock_id),
  UNIQUE KEY uk_ticket_stock_type (type_id),
  KEY idx_ticket_stock_session (session_id),
  KEY idx_ticket_stock_activity (activity_id),
  KEY idx_ticket_stock_available (type_id, available_stock)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务库存表';

-- ----------------------------
-- 8、库存流水表
-- ----------------------------
CREATE TABLE ticket_stock_log (
  log_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '库存流水ID',
  stock_id         bigint(20)      NOT NULL COMMENT '库存ID',
  activity_id      bigint(20)      NOT NULL COMMENT '活动ID',
  session_id       bigint(20)      NOT NULL COMMENT '场次ID',
  type_id          bigint(20)      NOT NULL COMMENT '票种ID',
  order_no         varchar(64)      DEFAULT '' COMMENT '订单号',
  change_type      char(1)          NOT NULL COMMENT '变更类型（0初始化 1锁定 2支付扣减 3取消释放 4退款回补 5人工调整）',
  change_qty       int(11)          NOT NULL COMMENT '变更数量，正数增加，负数减少',
  before_qty       int(11)          DEFAULT 0 COMMENT '变更前可售库存',
  after_qty        int(11)          DEFAULT 0 COMMENT '变更后可售库存',
  operator_id      bigint(20)       DEFAULT NULL COMMENT '操作人ID',
  operator_name    varchar(64)      DEFAULT '' COMMENT '操作人名称',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (log_id),
  KEY idx_ticket_stock_log_stock (stock_id),
  KEY idx_ticket_stock_log_order (order_no),
  KEY idx_ticket_stock_log_type_time (type_id, create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务库存流水表';

-- ----------------------------
-- 9、订单表
-- ----------------------------
CREATE TABLE ticket_order (
  order_id         bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  order_no         varchar(64)      NOT NULL COMMENT '订单号',
  user_id          bigint(20)       NOT NULL COMMENT '小程序用户ID',
  openid           varchar(64)      NOT NULL COMMENT '微信openid',
  activity_id      bigint(20)       NOT NULL COMMENT '活动ID',
  session_id       bigint(20)       NOT NULL COMMENT '场次ID',
  order_title      varchar(200)     DEFAULT '' COMMENT '订单标题',
  total_quantity   int(11)          NOT NULL DEFAULT 0 COMMENT '购票数量',
  total_amount     decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  discount_amount  decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  pay_amount       decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '实付金额',
  pay_status       char(1)          DEFAULT '0' COMMENT '支付状态（0未支付 1已支付 2已退款）',
  order_status     char(1)          DEFAULT '0' COMMENT '订单状态（0待支付 1已支付 2已取消 3已超时 4已退款 5已核销）',
  source           char(1)          DEFAULT '0' COMMENT '订单来源（0微信小程序 1后台）',
  client_ip        varchar(128)     DEFAULT '' COMMENT '客户端IP',
  expire_time      datetime         DEFAULT NULL COMMENT '支付截止时间',
  pay_time         datetime         DEFAULT NULL COMMENT '支付时间',
  cancel_time      datetime         DEFAULT NULL COMMENT '取消时间',
  refund_time      datetime         DEFAULT NULL COMMENT '退款时间',
  checkin_time     datetime         DEFAULT NULL COMMENT '核销时间',
  transaction_id   varchar(64)      DEFAULT '' COMMENT '微信支付交易号',
  prepay_id        varchar(128)     DEFAULT '' COMMENT '微信预支付ID',
  create_by        varchar(64)      DEFAULT '' COMMENT '创建者',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_by        varchar(64)      DEFAULT '' COMMENT '更新者',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (order_id),
  UNIQUE KEY uk_ticket_order_no (order_no),
  KEY idx_ticket_order_user_status (user_id, order_status),
  KEY idx_ticket_order_openid (openid),
  KEY idx_ticket_order_activity (activity_id),
  KEY idx_ticket_order_session (session_id),
  KEY idx_ticket_order_expire (order_status, expire_time),
  KEY idx_ticket_order_transaction (transaction_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务订单表';

-- ----------------------------
-- 10、订单明细表
-- ----------------------------
CREATE TABLE ticket_order_item (
  item_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  order_id         bigint(20)       NOT NULL COMMENT '订单ID',
  order_no         varchar(64)      NOT NULL COMMENT '订单号',
  user_id          bigint(20)       NOT NULL COMMENT '小程序用户ID',
  activity_id      bigint(20)       NOT NULL COMMENT '活动ID',
  session_id       bigint(20)       NOT NULL COMMENT '场次ID',
  type_id          bigint(20)       NOT NULL COMMENT '票种ID',
  type_name        varchar(100)     NOT NULL COMMENT '票种名称',
  ticket_no        varchar(64)      DEFAULT '' COMMENT '票号',
  passenger_id     bigint(20)       DEFAULT NULL COMMENT '购票人ID',
  passenger_name   varchar(50)      DEFAULT '' COMMENT '购票人姓名',
  id_type          char(1)          DEFAULT '0' COMMENT '证件类型（0身份证 1护照 2其他）',
  id_no            varchar(64)      DEFAULT '' COMMENT '证件号码',
  seat_area        varchar(50)      DEFAULT '' COMMENT '区域',
  seat_row         varchar(50)      DEFAULT '' COMMENT '排号',
  seat_no          varchar(50)      DEFAULT '' COMMENT '座位号',
  price            decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '单价',
  quantity         int(11)          NOT NULL DEFAULT 1 COMMENT '数量',
  item_amount      decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '明细金额',
  item_status      char(1)          DEFAULT '0' COMMENT '明细状态（0待支付 1有效 2已取消 3已退款 4已核销）',
  checkin_time     datetime         DEFAULT NULL COMMENT '核销时间',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (item_id),
  UNIQUE KEY uk_ticket_order_item_ticket_no (ticket_no),
  KEY idx_ticket_order_item_order (order_id),
  KEY idx_ticket_order_item_order_no (order_no),
  KEY idx_ticket_order_item_user (user_id),
  KEY idx_ticket_order_item_type (type_id),
  KEY idx_ticket_order_item_passenger (passenger_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务订单明细表';

-- ----------------------------
-- 11、核销码表
-- ----------------------------
CREATE TABLE ticket_checkin_code (
  code_id          bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '核销码ID',
  order_id         bigint(20)       NOT NULL COMMENT '订单ID',
  order_no         varchar(64)      NOT NULL COMMENT '订单号',
  item_id          bigint(20)       DEFAULT NULL COMMENT '订单明细ID',
  ticket_no        varchar(64)      DEFAULT '' COMMENT '票号',
  checkin_code     varchar(128)     NOT NULL COMMENT '核销码',
  qrcode_url       varchar(500)     DEFAULT '' COMMENT '二维码地址',
  status           char(1)          DEFAULT '0' COMMENT '状态（0未核销 1已核销 2已作废）',
  checkin_user_id  bigint(20)       DEFAULT NULL COMMENT '核销人ID',
  checkin_user     varchar(64)      DEFAULT '' COMMENT '核销人',
  checkin_time     datetime         DEFAULT NULL COMMENT '核销时间',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (code_id),
  UNIQUE KEY uk_ticket_checkin_code (checkin_code),
  KEY idx_ticket_checkin_order (order_no),
  KEY idx_ticket_checkin_item (item_id),
  KEY idx_ticket_checkin_status (status)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务核销码表';

-- ----------------------------
-- 12、支付日志表
-- ----------------------------
CREATE TABLE ticket_pay_log (
  pay_id           bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '支付日志ID',
  order_id         bigint(20)       NOT NULL COMMENT '订单ID',
  order_no         varchar(64)      NOT NULL COMMENT '订单号',
  pay_no           varchar(64)      NOT NULL COMMENT '支付流水号',
  user_id          bigint(20)       NOT NULL COMMENT '小程序用户ID',
  openid           varchar(64)      NOT NULL COMMENT '微信openid',
  pay_channel      char(1)          DEFAULT '0' COMMENT '支付渠道（0微信支付）',
  pay_amount       decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '支付金额',
  pay_status       char(1)          DEFAULT '0' COMMENT '支付状态（0待支付 1成功 2失败 3关闭）',
  transaction_id   varchar(64)      DEFAULT '' COMMENT '微信支付交易号',
  prepay_id        varchar(128)     DEFAULT '' COMMENT '微信预支付ID',
  notify_time      datetime         DEFAULT NULL COMMENT '回调时间',
  notify_body      text             COMMENT '回调报文',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (pay_id),
  UNIQUE KEY uk_ticket_pay_no (pay_no),
  KEY idx_ticket_pay_order (order_no),
  KEY idx_ticket_pay_transaction (transaction_id),
  KEY idx_ticket_pay_user (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务支付日志表';

-- ----------------------------
-- 13、退款日志表
-- ----------------------------
CREATE TABLE ticket_refund_log (
  refund_id        bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '退款日志ID',
  order_id         bigint(20)       NOT NULL COMMENT '订单ID',
  order_no         varchar(64)      NOT NULL COMMENT '订单号',
  refund_no        varchar(64)      NOT NULL COMMENT '商户退款单号',
  transaction_id   varchar(64)      DEFAULT '' COMMENT '微信支付交易号',
  refund_id_wx     varchar(64)      DEFAULT '' COMMENT '微信退款单号',
  user_id          bigint(20)       NOT NULL COMMENT '小程序用户ID',
  refund_amount    decimal(10,2)    NOT NULL DEFAULT 0.00 COMMENT '退款金额',
  refund_reason    varchar(255)     DEFAULT '' COMMENT '退款原因',
  refund_status    char(1)          DEFAULT '0' COMMENT '退款状态（0申请中 1成功 2失败 3关闭）',
  notify_time      datetime         DEFAULT NULL COMMENT '回调时间',
  notify_body      text             COMMENT '回调报文',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (refund_id),
  UNIQUE KEY uk_ticket_refund_no (refund_no),
  KEY idx_ticket_refund_order (order_no),
  KEY idx_ticket_refund_transaction (transaction_id),
  KEY idx_ticket_refund_user (user_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务退款日志表';

-- ----------------------------
-- 14、订阅通知表
-- ----------------------------
CREATE TABLE ticket_notify_subscribe (
  subscribe_id     bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '订阅ID',
  user_id          bigint(20)       NOT NULL COMMENT '小程序用户ID',
  openid           varchar(64)      NOT NULL COMMENT '微信openid',
  activity_id      bigint(20)       DEFAULT NULL COMMENT '活动ID',
  session_id       bigint(20)       DEFAULT NULL COMMENT '场次ID',
  template_id      varchar(128)     DEFAULT '' COMMENT '订阅消息模板ID',
  notify_type      char(1)          DEFAULT '0' COMMENT '通知类型（0开售提醒 1支付提醒 2出票提醒）',
  status           char(1)          DEFAULT '0' COMMENT '状态（0待发送 1已发送 2发送失败 3已取消）',
  subscribe_time   datetime         DEFAULT NULL COMMENT '订阅时间',
  send_time        datetime         DEFAULT NULL COMMENT '发送时间',
  fail_reason      varchar(500)     DEFAULT '' COMMENT '失败原因',
  create_time      datetime         DEFAULT NULL COMMENT '创建时间',
  update_time      datetime         DEFAULT NULL COMMENT '更新时间',
  remark           varchar(500)     DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (subscribe_id),
  KEY idx_ticket_notify_user (user_id),
  KEY idx_ticket_notify_openid (openid),
  KEY idx_ticket_notify_activity (activity_id),
  KEY idx_ticket_notify_status_time (status, subscribe_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='票务订阅通知表';

-- ----------------------------
-- 初始化示例数据
-- ----------------------------
INSERT INTO ticket_config(config_key, config_value, config_name, config_type, status, create_by, create_time, remark)
SELECT 'ticket.order.timeout.seconds', '900', '订单支付超时时间', '0', '0', 'admin', SYSDATE(), '单位：秒'
WHERE NOT EXISTS (SELECT 1 FROM ticket_config WHERE config_key = 'ticket.order.timeout.seconds');

INSERT INTO ticket_activity(
  activity_name, activity_code, activity_type, cover_url, venue_name, venue_address,
  sale_start_time, sale_end_time, start_time, end_time, purchase_limit,
  need_real_name, need_seat, order_timeout, status, description, create_by, create_time, remark
)
SELECT
  '示例演出活动', 'ACT202607010001', '0', '', '示例剧场', '示例地址',
  '2026-07-01 10:00:00', '2026-07-30 18:00:00', '2026-08-01 19:30:00', '2026-08-01 21:30:00', 2,
  '1', '0', 900, '1', '这是一个抢票小程序示例活动。', 'admin', SYSDATE(), '示例数据'
WHERE NOT EXISTS (SELECT 1 FROM ticket_activity WHERE activity_code = 'ACT202607010001');

INSERT INTO ticket_session(
  activity_id, session_name, session_code, session_start, session_end,
  sale_start_time, sale_end_time, venue_name, total_stock, sold_stock, locked_stock, status, sort, create_by, create_time, remark
)
SELECT
  a.activity_id, '8月1日晚场', 'SES202608010001', '2026-08-01 19:30:00', '2026-08-01 21:30:00',
  '2026-07-01 10:00:00', '2026-07-30 18:00:00', '示例剧场', 100, 0, 0, '1', 1, 'admin', SYSDATE(), '示例场次'
FROM ticket_activity a
WHERE a.activity_code = 'ACT202607010001'
  AND NOT EXISTS (SELECT 1 FROM ticket_session WHERE session_code = 'SES202608010001');

INSERT INTO ticket_type(
  activity_id, session_id, type_name, type_code, price, market_price,
  total_stock, sold_stock, locked_stock, min_buy, max_buy, status, sort, create_by, create_time, remark
)
SELECT
  s.activity_id, s.session_id, '普通票', 'TYPE202608010001', 99.00, 99.00,
  100, 0, 0, 1, 2, '0', 1, 'admin', SYSDATE(), '示例票种'
FROM ticket_session s
WHERE s.session_code = 'SES202608010001'
  AND NOT EXISTS (SELECT 1 FROM ticket_type WHERE type_code = 'TYPE202608010001');

INSERT INTO ticket_stock(activity_id, session_id, type_id, total_stock, available_stock, locked_stock, sold_stock, status, create_by, create_time, remark)
SELECT t.activity_id, t.session_id, t.type_id, 100, 100, 0, 0, '0', 'admin', SYSDATE(), '示例库存'
FROM ticket_type t
WHERE t.type_code = 'TYPE202608010001'
  AND NOT EXISTS (SELECT 1 FROM ticket_stock WHERE type_id = t.type_id);

SET FOREIGN_KEY_CHECKS = 1;
