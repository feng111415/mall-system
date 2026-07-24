-- 本机 Navicat 初始化脚本
-- 执行顺序：先建库并切换到 ry-vue，再依次导入本目录下的 ry_20260417.sql、quartz.sql。
--
-- 如果用 Navicat 图形界面：
-- 1. 新建连接，主机 127.0.0.1，端口 3306，账号 root，密码 123456。
-- 2. 打开查询，新建并执行下面三行。
-- 3. 右键 ry-vue 数据库，运行 SQL 文件，按上面的顺序导入两个文件。

create database if not exists `ry-vue` default character set utf8mb4 collate utf8mb4_general_ci;
use `ry-vue`;
set names utf8mb4;
