CREATE TABLE `t_group_0` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_1` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_2` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_3` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_4` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_5` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_6` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_7` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_8` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_9` (
     `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
     `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
     `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
     `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
     `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
     `create_time` datetime DEFAULT NULL COMMENT '创建时间',
     `update_time` datetime DEFAULT NULL COMMENT '修改时间',
     `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
     PRIMARY KEY (`id`),
     UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_10` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
    `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_11` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
    `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_12` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
    `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_13` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
    `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_14` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
    `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `t_group_15` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    `name` varchar(64) DEFAULT NULL COMMENT '分组名称',
    `username` varchar(256) DEFAULT NULL COMMENT '创建分组用户名',
    `sort_order` int(3) DEFAULT NULL COMMENT '分组排序',
    `create_time` datetime DEFAULT NULL COMMENT '创建时间',
    `update_time` datetime DEFAULT NULL COMMENT '修改时间',
    `del_flag` tinyint(1) DEFAULT NULL COMMENT '删除标识 0：未删除 1：已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_username_gid` (`gid`,`username`) USING BTREE # 最左匹配原则，把常用的索引字段放在左边
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;