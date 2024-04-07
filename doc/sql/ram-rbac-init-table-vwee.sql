
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tb_rbac_config
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_config`;
CREATE TABLE `tb_rbac_config`  (
  `id` bigint(19) NOT NULL COMMENT 'id',
  `key_code` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '键名',
  `key_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL COMMENT '键值',
  `key_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '简介',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_config_key_code`(`key_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_croatian_ci COMMENT = '系统K/V字典' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_rbac_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_permission`;
CREATE TABLE `tb_rbac_permission`  (
  `id` bigint(19) NOT NULL COMMENT 'ID',
  `permission_type` tinyint(2) NOT NULL COMMENT '权限类型',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '权限编码',
  `permission_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限名称',
  `pid` bigint(19) NULL DEFAULT NULL COMMENT '上级权限ID',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_permission_permission_code`(`permission_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '权限信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_rbac_role
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_role`;
CREATE TABLE `tb_rbac_role`  (
  `id` bigint(19) NOT NULL COMMENT 'ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `role_default` tinyint(1) NULL DEFAULT NULL COMMENT '是否默认角色',
  `role_desc` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '角色介绍',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_role_role_code`(`role_code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_rbac_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_role_permission`;
CREATE TABLE `tb_rbac_role_permission`  (
  `id` bigint(19) NOT NULL COMMENT 'ID',
  `role_id` bigint(19) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(19) NOT NULL COMMENT '权限ID',
  `permission_type` tinyint(2) NOT NULL COMMENT '权限类型',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_rp_role_id_permission_id`(`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_rbac_role_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_role_user`;
CREATE TABLE `tb_rbac_role_user`  (
  `id` bigint(19) NOT NULL COMMENT 'ID',
  `role_id` bigint(19) NOT NULL COMMENT '角色ID',
  `user_id` bigint(19) NOT NULL COMMENT '用户ID',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_ru_role_id_user_id`(`role_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色用户关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_rbac_ticket
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_ticket`;
CREATE TABLE `tb_rbac_ticket`  (
  `id` bigint(19) NOT NULL COMMENT 'id',
  `user_from` tinyint(2) NULL DEFAULT NULL COMMENT '用户来源',
  `user_id` bigint(19) NULL DEFAULT NULL COMMENT '用户ID',
  `ticket` varchar(38) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '凭证',
  `client_type` tinyint(2) NULL DEFAULT NULL COMMENT '客户端类型',
  `login_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '登录方式',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT 'ip地址',
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '用户UA',
  `account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '账号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT 'email',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '电话号码',
  `nick_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `last_refresh_time` timestamp(0) NULL DEFAULT NULL COMMENT '上次刷新时间',
  `time_expire` timestamp(0) NULL DEFAULT NULL COMMENT '预估失效时间',
  `client_hash` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL DEFAULT NULL COMMENT '客户端hash标识',
  `client_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_croatian_ci NULL COMMENT '客户端信息',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_ticket_ticket`(`ticket`) USING BTREE,
  INDEX `idx_rbac_ticket_user_id`(`user_id`) USING BTREE,
  INDEX `idx_rbac_ticket_account`(`account`) USING BTREE,
  INDEX `idx_rbac_ticket_email`(`email`) USING BTREE,
  INDEX `idx_rbac_ticket_phone_number`(`phone_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_croatian_ci COMMENT = '用户凭证信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for tb_rbac_user
-- ----------------------------
DROP TABLE IF EXISTS `tb_rbac_user`;
CREATE TABLE `tb_rbac_user`  (
  `id` bigint(19) NOT NULL COMMENT 'ID',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `password` varchar(71) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '密码',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱地址',
  `phone_number` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '电话号码',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '昵称',
  `account_state` tinyint(2) NULL DEFAULT NULL COMMENT '帐号状态',
  `user_from` tinyint(2) NULL DEFAULT NULL COMMENT '用户来源',
  `create_time` timestamp(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_rbac_user_account`(`account`) USING BTREE,
  UNIQUE INDEX `uk_rbac_user_email`(`email`) USING BTREE,
  UNIQUE INDEX `uk_rbac_user_phone_number`(`phone_number`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户信息' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
