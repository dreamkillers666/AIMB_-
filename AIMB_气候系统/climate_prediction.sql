/*
 Navicat Premium Data Transfer

 Source Server         : root
 Source Server Type    : MySQL
 Source Server Version : 50719
 Source Host           : localhost:3306
 Source Schema         : climate_prediction

 Target Server Type    : MySQL
 Target Server Version : 50719
 File Encoding         : 65001

 Date: 30/10/2024 18:11:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '公告id',
  `title` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '标题',
  `release_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '发布时间',
  `content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '内容',
  `author` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '发布者',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Compact;

-- ----------------------------
-- Records of announcement
-- ----------------------------
INSERT INTO `announcement` VALUES (16, 'fsaewa', '2020-03-25 00:00:00', '新公告', '管理员');
INSERT INTO `announcement` VALUES (17, 'fewaf', '2020-03-25 00:00:00', '新公告', '管理员');
INSERT INTO `announcement` VALUES (18, 'feawfawf', '2020-03-25 00:00:00', '新公告', '管理员');
INSERT INTO `announcement` VALUES (19, 'aefa', '2020-03-25 00:00:00', '新公告', '管理员');
INSERT INTO `announcement` VALUES (20, 'aewfaf', '2020-03-25 00:00:00', '\n重要通知：公司年会定于明天下午2点开始，请全体员工准时参加。', '管理员');
INSERT INTO `announcement` VALUES (21, '发布公告', '2020-05-07 00:00:00', '	\n关于工资调整的内容，我司将决定对全体员工的工资在之前的基础上加500元，以鼓励大家对公司的贡献，请全体员工再接再厉，为我司再创辉煌！！！关于工资调整的内容，我司将决定对全体员工的工资在之前的基础上加500元，以鼓励大家对公司的贡献，请全体员工再接再厉，为我司再创辉煌！！！关于工资调整的内容，我司将决定对全体员工的工资在之前的基础上加500元，以鼓励大家对公司的贡献', 'Admin');

-- ----------------------------
-- Table structure for attendance
-- ----------------------------
DROP TABLE IF EXISTS `attendance`;
CREATE TABLE `attendance`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '员工姓名',
  `work_id` char(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号',
  `attendance_date` date NULL DEFAULT NULL COMMENT '考勤日期',
  `start_time` double(4, 1) NULL DEFAULT NULL COMMENT '上班时间',
  `end_time` double(4, 1) NULL DEFAULT NULL COMMENT '下班时间',
  `work_hours` double(4, 1) NULL DEFAULT NULL COMMENT '工作时长',
  `absence_times` int(4) NULL DEFAULT NULL COMMENT '缺勤次数（每月）',
  `late_early_times` int(4) NULL DEFAULT NULL COMMENT '迟到早退次数（每月）',
  `is_sign` tinyint(1) NULL DEFAULT 0 COMMENT '是否签到',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of attendance
-- ----------------------------
INSERT INTO `attendance` VALUES (1, '张三', 'AD001', '2023-01-04', 8.5, 16.5, 8.0, 0, 2, 1);
INSERT INTO `attendance` VALUES (2, '李四', 'AD002', '2023-02-08', 8.5, 17.0, 8.5, 1, 3, 0);
INSERT INTO `attendance` VALUES (3, '李韵', '0D009', '2022-06-07', 8.0, 17.0, 9.0, 0, 0, 0);
INSERT INTO `attendance` VALUES (4, '李雯', 'SD005', '2023-05-14', 8.0, 17.0, 9.0, 0, 0, 0);
INSERT INTO `attendance` VALUES (5, '洪强国', '0D010', '2023-05-01', 8.0, 17.0, 9.0, 0, 0, 0);

-- ----------------------------
-- Table structure for building
-- ----------------------------
DROP TABLE IF EXISTS `building`;
CREATE TABLE `building`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '产业名称',
  `longitude` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '经度',
  `latitude` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '纬度',
  `icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '图标链接',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of building
-- ----------------------------
INSERT INTO `building` VALUES (1, '南审', '118.614913', '32.068569', '//vdata.amap.com/icons/b18/1/2.png');
INSERT INTO `building` VALUES (2, '南工', '118.647258', '32.088423', '//vdata.amap.com/icons/b18/1/2.png');
INSERT INTO `building` VALUES (3, '南农', '118.705705', '32.139064', '//vdata.amap.com/icons/b18/1/2.png');
INSERT INTO `building` VALUES (5, '南信', '118.721777', '32.209229', '//vdata.amap.com/icons/b18/1/2.png');

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '评论人id',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评论时间',
  `pid` int(11) NULL DEFAULT NULL COMMENT '父id',
  `origin_id` int(11) NULL DEFAULT NULL COMMENT '最上级评论id',
  `feedback_id` int(11) NULL DEFAULT NULL COMMENT '关联文章的id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------
INSERT INTO `comment` VALUES (1, '测试', 18, '2022-03-22 20:00:00', NULL, NULL, 11);
INSERT INTO `comment` VALUES (2, '长度', 18, '2023-05-18 16:53:56', NULL, NULL, 11);
INSERT INTO `comment` VALUES (3, '单位·', 18, '2023-05-18 16:54:04', 2, 2, 11);
INSERT INTO `comment` VALUES (4, '很好', 18, '2023-06-03 21:51:35', NULL, NULL, 14);
INSERT INTO `comment` VALUES (5, '很好', 18, '2023-06-03 21:51:42', NULL, NULL, 13);
INSERT INTO `comment` VALUES (6, '很好', 18, '2023-06-03 21:51:47', NULL, NULL, 12);
INSERT INTO `comment` VALUES (7, '非常好', 26, '2023-06-03 21:52:10', NULL, NULL, 14);
INSERT INTO `comment` VALUES (8, '非常好', 26, '2023-06-03 21:52:30', NULL, NULL, 13);
INSERT INTO `comment` VALUES (9, '非常好', 26, '2023-06-03 21:52:46', NULL, NULL, 12);
INSERT INTO `comment` VALUES (11, '太棒了', 42, '2023-06-03 22:03:59', 7, 7, 14);
INSERT INTO `comment` VALUES (12, '太棒了', 42, '2023-06-03 22:05:14', 8, 8, 13);
INSERT INTO `comment` VALUES (13, '太棒了', 42, '2023-06-03 22:05:19', 9, 9, 12);
INSERT INTO `comment` VALUES (14, '6666', 42, '2023-06-03 22:05:28', NULL, NULL, 12);

-- ----------------------------
-- Table structure for contract
-- ----------------------------
DROP TABLE IF EXISTS `contract`;
CREATE TABLE `contract`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '合同编号',
  `work_id` char(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号',
  `contract_term` double NULL DEFAULT NULL COMMENT '合同期限',
  `begin_contract` date NULL DEFAULT NULL COMMENT '合同起始日期',
  `end_contract` date NULL DEFAULT NULL COMMENT '合同终止日期',
  `contract_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '合同内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of contract
-- ----------------------------
INSERT INTO `contract` VALUES (1, 'AD001', 10, '2011-07-05', '2021-08-08', 'xxxxxxxxxxxxxxxxxx');
INSERT INTO `contract` VALUES (4, 'AD002', 10, '2011-07-04', '2021-08-18', 'xxxxxxxxxxxxxxxxxx22');

-- ----------------------------
-- Table structure for emp
-- ----------------------------
DROP TABLE IF EXISTS `emp`;
CREATE TABLE `emp`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '员工编号',
  `name` varchar(10) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '员工姓名',
  `age` int(10) NULL DEFAULT NULL COMMENT '年龄',
  `gender` char(4) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '性别',
  `id_card` char(18) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '身份证号',
  `nation` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '民族',
  `email` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '电话号码',
  `address` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '联系地址',
  `dept` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '所属部门',
  `postion` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '职位',
  `degree` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '最高学历',
  `edate` date NULL DEFAULT NULL COMMENT '入职日期',
  `work_state` enum('在职','离职') CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT '在职' COMMENT '在职状态',
  `work_id` char(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号',
  `conversion_time` date NULL DEFAULT NULL COMMENT '转正日期',
  `work_age` int(11) NULL DEFAULT NULL COMMENT '工龄',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '员工' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of emp
-- ----------------------------
INSERT INTO `emp` VALUES (1, '张三', 35, '男', '456321754215896354', '汉族', 'zhangsan@163.com', '17452135841', 'dsadsfvgbnh', '行政部', '总监', '研究生', '2020-06-12', '在职', 'AD001', '2020-07-04', 3);
INSERT INTO `emp` VALUES (2, '李四', 36, '男', '489653214754123586', '汉族', 'lisi@163.com', '13564721547', 'cwdhetdm', '行政部', '总监', '研究生', '2020-06-04', '在职', 'AD002', '2020-07-04', 3);
INSERT INTO `emp` VALUES (3, '张强', 28, '男', '542135874521578945', '汉族', 'zhangq@163.com', '14785426357', 'xxxxxxxxxxxxxxx', '财务部', '助理', '研究生', '2020-06-04', '在职', 'FD003', '2020-07-04', 3);
INSERT INTO `emp` VALUES (5, '李雯', 25, '女', '440102198001021230', '汉族', 'liwei@163.com', '13526541234', 'xxxxxxxxx', '财务部', '经理', '研究生', '2022-06-04', '在职', 'FD004', '2022-07-04', 1);
INSERT INTO `emp` VALUES (6, '王思达', 37, '男', '452136198301066985', '汉族', 'wsd@163.com', '17452136854', 'sjngvbc', '销售部', '主管', '大专', '2021-06-04', '在职', 'SD005', '2021-07-04', 2);
INSERT INTO `emp` VALUES (7, '赵芳', 29, '女', '458712354621586324', '汉族', 'zf@163.com', '13542687942', 'tfgcvggh', '销售部', '普通员工', '大专', '2021-06-04', '在职', 'SD006', '2021-07-04', 2);
INSERT INTO `emp` VALUES (8, '邹好', 26, '女', '440102198001021230', '汉族', 'zhao@163.com', '16542314785', 'ghskcbwhe', '行政部', '普通员工', '高中', '2021-06-04', '在职', 'AD007', '2021-07-04', 2);
INSERT INTO `emp` VALUES (9, '李韵', 34, '男', '440102198001021230', '汉族', 'liyun@163.com', '14523147852', 'wsdfgefv', '行政部', '普通员工', '研究生', '2021-06-04', '在职', 'AD008', '2021-07-04', 2);
INSERT INTO `emp` VALUES (10, '王桂芳', 39, '女', '440102198001021230', '汉族', 'wgh@163.com', '18452369785', 'wsdfvvefvcs', '营运部', '主管', '大专', '2022-06-04', '在职', 'OD009', '2022-07-04', 1);
INSERT INTO `emp` VALUES (11, '洪强国', 43, '男', '440102198001021230', '汉族', 'hgq@163.com', '15236987452', 'dfnxv4rt', '营运部', '助理', '大专', '2022-06-04', '在职', 'OD010', '2022-07-04', 1);
INSERT INTO `emp` VALUES (13, '刘安福', 45, '男', '440102198001021230', '汉族', 'laf@163.com', '15236984521', 'sdg7hhbb', '财务部', '普通员工', '本科', '2021-06-04', '在职', 'FD011', '2021-07-04', 2);
INSERT INTO `emp` VALUES (14, '李芳', 27, '女', '440102198001021230', '汉族', 'lifang@163.com', '15423578541', 'sdfebe', '行政部', '经理', '高中', '2022-06-04', '在职', 'AD012', '2022-07-04', 1);

-- ----------------------------
-- Table structure for feedback
-- ----------------------------
DROP TABLE IF EXISTS `feedback`;
CREATE TABLE `feedback`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '内容',
  `user` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布人',
  `time` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发布时间',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '反馈类型',
  `feedback_status` tinyint(1) NULL DEFAULT 0 COMMENT '审批状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback
-- ----------------------------

-- ----------------------------
-- Table structure for feedback_comment
-- ----------------------------
DROP TABLE IF EXISTS `feedback_comment`;
CREATE TABLE `feedback_comment`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '内容',
  `user_id` int(11) NULL DEFAULT NULL COMMENT '评论人id',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '评论时间',
  `pid` int(11) NULL DEFAULT NULL COMMENT '父id',
  `origin_id` int(11) NULL DEFAULT NULL COMMENT '最上级评论id',
  `feedback_id` int(11) NULL DEFAULT NULL COMMENT '关联文章的id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedback_comment
-- ----------------------------
INSERT INTO `feedback_comment` VALUES (1, '测试', 18, '2022-03-22 20:00:00', NULL, NULL, 11);
INSERT INTO `feedback_comment` VALUES (2, '长度', 18, '2023-05-18 16:53:56', NULL, NULL, 11);
INSERT INTO `feedback_comment` VALUES (3, '单位·', 18, '2023-05-18 16:54:04', 2, 2, 11);
INSERT INTO `feedback_comment` VALUES (4, '很好', 18, '2023-06-03 21:51:35', NULL, NULL, 14);
INSERT INTO `feedback_comment` VALUES (5, '很好', 18, '2023-06-03 21:51:42', NULL, NULL, 13);
INSERT INTO `feedback_comment` VALUES (6, '很好', 18, '2023-06-03 21:51:47', NULL, NULL, 12);
INSERT INTO `feedback_comment` VALUES (7, '非常好', 26, '2023-06-03 21:52:10', NULL, NULL, 14);
INSERT INTO `feedback_comment` VALUES (8, '非常好', 26, '2023-06-03 21:52:30', NULL, NULL, 13);
INSERT INTO `feedback_comment` VALUES (9, '非常好', 26, '2023-06-03 21:52:46', NULL, NULL, 12);
INSERT INTO `feedback_comment` VALUES (11, '太棒了', 42, '2023-06-03 22:03:59', 7, 7, 14);
INSERT INTO `feedback_comment` VALUES (12, '太棒了', 42, '2023-06-03 22:05:14', 8, 8, 13);
INSERT INTO `feedback_comment` VALUES (13, '太棒了', 42, '2023-06-03 22:05:19', 9, 9, 12);
INSERT INTO `feedback_comment` VALUES (14, '6666', 42, '2023-06-03 22:05:28', NULL, NULL, 12);

-- ----------------------------
-- Table structure for model
-- ----------------------------
DROP TABLE IF EXISTS `model`;
CREATE TABLE `model`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `mon1` double(11, 2) NULL DEFAULT NULL,
  `mon2` double(11, 2) NULL DEFAULT NULL,
  `mon3` double(11, 2) NULL DEFAULT NULL,
  `mon4` double(11, 2) NULL DEFAULT NULL,
  `mon5` double(11, 2) NULL DEFAULT NULL,
  `mon6` double(11, 2) NULL DEFAULT NULL,
  `mon7` double(11, 2) NULL DEFAULT NULL,
  `mon8` double(11, 2) NULL DEFAULT NULL,
  `mon9` double(11, 2) NULL DEFAULT NULL,
  `mon10` double(11, 2) NULL DEFAULT NULL,
  `mon11` double(11, 2) NULL DEFAULT NULL,
  `mon12` double(11, 2) NULL DEFAULT NULL,
  `mon13` double(11, 2) NULL DEFAULT NULL,
  `mon14` double(11, 2) NULL DEFAULT NULL,
  `mon15` double(11, 2) NULL DEFAULT NULL,
  `mon16` double(11, 2) NULL DEFAULT NULL,
  `mon17` double(11, 2) NULL DEFAULT NULL,
  `mon18` double(11, 2) NULL DEFAULT NULL,
  `mon19` double(11, 2) NULL DEFAULT NULL,
  `mon20` double(11, 2) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '员工工资' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of model
-- ----------------------------
INSERT INTO `model` VALUES (23, 'SINTEX_F', 0.89, 0.87, 0.83, 0.80, 0.75, 0.72, 0.70, 0.65, 0.63, 0.60, 0.55, 0.51, 0.48, 0.47, 0.46, 0.45, 0.40, 0.35, 0.32, 0.31);
INSERT INTO `model` VALUES (24, 'CNN', 0.93, 0.91, 0.88, 0.83, 0.80, 0.75, 0.71, 0.71, 0.70, 0.69, 0.65, 0.64, 0.63, 0.60, 0.58, 0.53, 0.51, 0.45, 0.41, 0.38);
INSERT INTO `model` VALUES (25, 'Ours', 0.94, 0.90, 0.86, 0.84, 0.83, 0.79, 0.77, 0.75, 0.75, 0.71, 0.66, 0.65, 0.63, 0.64, 0.63, 0.62, 0.60, 0.52, 0.48, 0.49);
INSERT INTO `model` VALUES (26, 'Transformer', 0.98, 0.94, 0.89, 0.85, 0.81, 0.76, 0.75, 0.74, 0.72, 0.70, 0.68, 0.66, 0.65, 0.62, 0.59, 0.55, 0.53, 0.49, 0.44, 0.41);
INSERT INTO `model` VALUES (27, 'GRU', 0.93, 0.91, 0.89, 0.86, 0.81, 0.78, 0.73, 0.68, 0.65, 0.61, 0.57, 0.51, 0.50, 0.48, 0.44, 0.41, 0.40, 0.38, 0.37, 0.32);
INSERT INTO `model` VALUES (28, 'STANet', 0.94, 0.91, 0.90, 0.88, 0.85, 0.80, 0.78, 0.72, 0.69, 0.64, 0.61, 0.57, 0.53, 0.51, 0.49, 0.48, 0.45, 0.43, 0.41, 0.40);

-- ----------------------------
-- Table structure for salary
-- ----------------------------
DROP TABLE IF EXISTS `salary`;
CREATE TABLE `salary`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
  `work_id` char(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '员工工号',
  `base_salary` double(11, 2) NULL DEFAULT NULL COMMENT '基础工资',
  `performance_bonus` double(11, 2) NULL DEFAULT NULL COMMENT '绩效奖金',
  `social_security` double(11, 2) NULL DEFAULT NULL COMMENT '社保费用',
  `actual_salary` double(11, 2) NULL DEFAULT NULL COMMENT '实际工资',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '员工工资' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of salary
-- ----------------------------
INSERT INTO `salary` VALUES (1, '张三', 'AD001', 7000.00, 1000.00, 1000.00, 7000.00);
INSERT INTO `salary` VALUES (2, '李四', 'AD002', 8000.00, 1200.00, 1000.00, 8200.00);
INSERT INTO `salary` VALUES (3, '张强', 'FD003', 8000.00, 1500.00, 1000.00, 8500.00);

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件名称',
  `type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件类型',
  `size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小(kb)',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '下载链接',
  `is_delete` tinyint(1) NULL DEFAULT 0 COMMENT '是否删除',
  `enable` tinyint(1) NULL DEFAULT 1 COMMENT '是否禁用',
  `md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '文件md5',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file
-- ----------------------------
INSERT INTO `sys_file` VALUES (3, '礼仪培训视频.mp4', 'mp4', 19838, 'http://localhost:9090/file/6ba5345ab45b42b183d8bc9ed8886bdc.mp4', 0, 1, '2ee22b44117563f3d2bc623c7a4ef85e');
INSERT INTO `sys_file` VALUES (4, '企业新员工安全基础知识培训.mp4', 'mp4', 10033, 'http://localhost:9090/file/ecabd6da99244e098903f85b6a488cbf.mp4', 0, 1, '81fd9687f8079ee12b0605a35488a018');
INSERT INTO `sys_file` VALUES (24, '张三.jpg', 'jpg', 7, 'http://localhost:9090/file/68af84af637b460c8a1f222945befb22.jpg', 1, 1, '9a2c4a49166f9d1921e42cff632f3d53');
INSERT INTO `sys_file` VALUES (25, '王五.jpg', 'jpg', 159, 'http://localhost:9090/file/09cf5bd3aca3471eaaf9709bbfb30ba1.jpg', 1, 1, 'ce5168b64f982fd1bc3ed4ee06b47d7b');
INSERT INTO `sys_file` VALUES (27, '管理员头像.jpg', 'jpg', 5, 'http://localhost:9090/file/fbb9bc0002db43a19251140e6249cb64.jpg', 1, 1, '016226024466e37eba220f1d9432e415');
INSERT INTO `sys_file` VALUES (28, '李四.jpg', 'jpg', 6, 'http://localhost:9090/file/c3beaf83655143b68ac36a0b77cc0c44.jpg', 1, 1, 'e1fe7759edb5530ec95edcfe53187ca0');
INSERT INTO `sys_file` VALUES (29, 'logo.png', 'png', 1, 'http://localhost:9090/file/e3984d92b67f4f06a56a7396e8dc54bf.png', 0, 1, '9f871dd8cbb6cb2801ec3a2877accd23');
INSERT INTO `sys_file` VALUES (30, '张三.jpg', 'jpg', 6, 'http://localhost:9090/file/c3beaf83655143b68ac36a0b77cc0c44.jpg', 0, 1, 'e1fe7759edb5530ec95edcfe53187ca0');
INSERT INTO `sys_file` VALUES (31, '李四.jpg', 'jpg', 7, 'http://localhost:9090/file/68af84af637b460c8a1f222945befb22.jpg', 0, 1, '9a2c4a49166f9d1921e42cff632f3d53');
INSERT INTO `sys_file` VALUES (32, '管理员.jpg', 'jpg', 5, 'http://localhost:9090/file/fbb9bc0002db43a19251140e6249cb64.jpg', 0, 1, '016226024466e37eba220f1d9432e415');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `create_time` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `avatar_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像路径',
  `type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '用户类型',
  `work_id` char(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工号',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (18, '张三', '123456', '2qqtest789@qq.com', '213456789012', '江苏徐州', '2022-03-29 16:59:44', 'http://localhost:9090/file/b4a88592a10a46b6b06ea53702ca222c.jpg', '用户', 'AD001');
INSERT INTO `sys_user` VALUES (26, '李四', '123456', 'qqlistener@qq.com', '18723456789', '浙江杭州', '2022-07-08 17:20:01', 'http://localhost:9090/file/09cf5bd3aca3471eaaf9709bbfb30ba1.jpg', '用户', 'SD005');
INSERT INTO `sys_user` VALUES (42, '王五', '123456', 'email@hotmail.com', '13123456789', '湖南长沙', '2023-04-19 22:20:50', 'http://localhost:9090/file/68af84af637b460c8a1f222945befb22.jpg', '用户', 'SD006');
INSERT INTO `sys_user` VALUES (43, 'admin', '123456', 'user123@yahoo.com', '15987654321', '江苏镇江', '2023-05-01 19:58:56', 'http://localhost:9090/file/fbb9bc0002db43a19251140e6249cb64.jpg', '管理员', 'AD012');
INSERT INTO `sys_user` VALUES (44, '里斯多', '123456', 'example1@gmail.com', '13812345678', '河南郑州', '2023-05-01 20:00:39', NULL, '用户', 'OD009');
INSERT INTO `sys_user` VALUES (45, '赵六', '123456', '2651479762x@gmail.com', '19876543211', '江苏徐州', '2023-05-01 20:40:45', NULL, '用户', 'OD010');

-- ----------------------------
-- Table structure for train
-- ----------------------------
DROP TABLE IF EXISTS `train`;
CREATE TABLE `train`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `work_id` char(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号',
  `train_date` date NULL DEFAULT NULL COMMENT '培训日期',
  `train_content` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '培训内容',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of train
-- ----------------------------
INSERT INTO `train` VALUES (1, 'FD003', '2023-04-22', '礼仪培训', '礼仪培训视频.mp4');
INSERT INTO `train` VALUES (2, 'OD010', '2023-05-01', '安全训练', '企业新员工安全基础知识培训.mp4');

-- ----------------------------
-- Table structure for vacation
-- ----------------------------
DROP TABLE IF EXISTS `vacation`;
CREATE TABLE `vacation`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '员工姓名',
  `work_id` char(8) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '工号',
  `leave_date` date NULL DEFAULT NULL COMMENT '请假日期',
  `leave_days` int(4) NULL DEFAULT NULL COMMENT '请假天数',
  `back_date` date NULL DEFAULT NULL COMMENT '补班日期',
  `vacation_type` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请假类型',
  `vacation_reason` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT '请假原因',
  `audit_status` tinyint(1) NULL DEFAULT 0 COMMENT '审核状态',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of vacation
-- ----------------------------
INSERT INTO `vacation` VALUES (1, '张三', 'AD001', '2023-02-11', 3, '2023-03-15', '病假', '生病', 1);
INSERT INTO `vacation` VALUES (2, '李四', 'AD002', '2023-03-09', 10, '2023-04-13', '婚假', '结婚', -1);
INSERT INTO `vacation` VALUES (7, '王五', 'SD005', '2023-05-01', 9, '2023-05-25', '丧假', NULL, 1);
INSERT INTO `vacation` VALUES (9, '张三', 'AD001', '2023-06-07', 5, '2023-06-23', '产假', NULL, 1);

SET FOREIGN_KEY_CHECKS = 1;
