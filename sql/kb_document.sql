USE `ry-vue`;

CREATE TABLE kb_document (
    document_id    BIGINT  PRIMARY KEY AUTO_INCREMENT,
    category_id    BIGINT  NOT NULL,
    document_name  VARCHAR(200)  NOT NULL,
    file_name      VARCHAR(255),
    file_path      VARCHAR(500),
    file_type      VARCHAR(50),
    status         CHAR(1) DEFAULT '0',
    create_time    DATETIME,
    update_time    DATETIME,
    remark         VARCHAR(500)
);