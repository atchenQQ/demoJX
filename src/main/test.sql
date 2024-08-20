/*
 Navicat Premium Data Transfer

 Source Server         : AI
 Source Server Type    : MySQL
 Source Server Version : 80036
 Source Host           : localhost:3303
 Source Schema         : test

 Target Server Type    : MySQL
 Target Server Version : 80036
 File Encoding         : 65001

 Date: 20/08/2024 18:05:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `dept_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `dept_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES (1, '技术部');
INSERT INTO `department` VALUES (2, '销售部');
INSERT INTO `department` VALUES (3, '市场部');
INSERT INTO `department` VALUES (4, '客服部');
INSERT INTO `department` VALUES (5, '财务部');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '工单id',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工单编号',
  `order_type` tinyint NOT NULL COMMENT '工单类型 0交办 1直接答复 3无效工单',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
  `handle_dept_id` bigint NULL DEFAULT NULL COMMENT '处理部门',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `fenpai_time` datetime NULL DEFAULT NULL COMMENT '分派时间',
  `is_overdue` tinyint NOT NULL DEFAULT 0 COMMENT '是否超期 0否 1是',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES (1, 'ORD001', 0, '技术问题', '服务器无法启动', 1, '2023-01-01 10:00:00', '2023-01-02 10:00:00', 0);
INSERT INTO `order` VALUES (2, 'ORD002', 1, '销售咨询', '产品价格查询', 2, '2023-01-02 11:00:00', NULL, 0);
INSERT INTO `order` VALUES (3, 'ORD003', 0, '市场活动', '新年促销活动', 3, '2023-01-03 12:00:00', '2023-01-04 12:00:00', 1);
INSERT INTO `order` VALUES (4, 'ORD004', 3, '无效工单', '重复提交的问题', NULL, '2023-01-04 13:00:00', NULL, 0);
INSERT INTO `order` VALUES (5, 'ORD005', 1, '客服反馈', '用户投诉处理', 4, '2023-01-05 14:00:00', '2023-01-06 14:00:00', 0);

SET FOREIGN_KEY_CHECKS = 1;
