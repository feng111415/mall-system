CREATE TABLE test_student(
    student_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_name VARCHAR(50),
    age INT,
    status CHAR(1),
    create_time DATETIME,
    update_time DATETIME,
    remark VARCHAR(500)
);