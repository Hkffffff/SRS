/*
 Navicat Premium Dump SQL

 Source Server         : webassignment
 Source Server Type    : MySQL
 Source Server Version : 90400 (9.4.0)
 Source Host           : localhost:3306
 Source Schema         : srs

 Target Server Type    : MySQL
 Target Server Version : 90400 (9.4.0)
 File Encoding         : 65001

 Date: 27/04/2026 09:39:37
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for blacklist
-- ----------------------------
DROP TABLE IF EXISTS `blacklist`;
CREATE TABLE `blacklist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '违规记录ID',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '违规人学号(外键)',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '违规原因(例如: 2026-04-15 上午时段预约未签到)',
  `violation_date` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '违规记录生成时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-封禁生效中，0-已解除封禁',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_black_student`(`student_id` ASC) USING BTREE,
  CONSTRAINT `fk_black_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`student_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '违约与信誉黑名单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for reservation
-- ----------------------------
DROP TABLE IF EXISTS `reservation`;
CREATE TABLE `reservation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约记录主键',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '预约人学号(外键)',
  `seat_id` bigint NOT NULL COMMENT '预约座位ID(外键)',
  `reservation_date` date NOT NULL COMMENT '预约日期',
  `time_slot` tinyint NOT NULL COMMENT '时间段：1-上午，2-下午，3-晚上',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0-已预约(待签到)，1-已签到，2-已取消，3-违约未到',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交预约的时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_res_student`(`student_id` ASC) USING BTREE,
  INDEX `fk_res_seat`(`seat_id` ASC) USING BTREE,
  CONSTRAINT `fk_res_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_res_student` FOREIGN KEY (`student_id`) REFERENCES `user` (`student_id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '座位预约业务流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for room
-- ----------------------------
DROP TABLE IF EXISTS `room`;
CREATE TABLE `room`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自习室ID',
  `room_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '自习室名称(如: 图书馆3楼东区)',
  `floor` int NOT NULL COMMENT '所在楼层',
  `total_seats` int NOT NULL COMMENT '总座位数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-开放，0-关闭维护',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '自习室物理资源表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for seat
-- ----------------------------
DROP TABLE IF EXISTS `seat`;
CREATE TABLE `seat`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '座位ID',
  `room_id` bigint NOT NULL COMMENT '所属自习室ID(外键)',
  `seat_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '座位编号(如: A-01)',
  `has_window` tinyint(1) NULL DEFAULT 0 COMMENT '是否靠窗：1-是，0-否',
  `has_power` tinyint(1) NULL DEFAULT 0 COMMENT '是否有插座：1-是，0-否',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1-正常可用，0-损坏维修中',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_seat_room`(`room_id` ASC) USING BTREE,
  CONSTRAINT `fk_seat_room` FOREIGN KEY (`room_id`) REFERENCES `room` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 113 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '座位详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `student_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号(唯一键)',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码(建议MD5加密存储)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `role` tinyint NOT NULL DEFAULT 0 COMMENT '角色：0-学生，1-管理员',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '账号创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
