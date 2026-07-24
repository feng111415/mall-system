USE `ry-vue`;
CREATE TABLE kb_category(
    category_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name  VARCHAR(100)  NOT NULL,
    order_num INT  DEFAULT 0,
    status CHAR(1)  DEFAULT '0',
    create_time    DATETIME,
    update_time    DATETIME,
    remark         VARCHAR(500)
);