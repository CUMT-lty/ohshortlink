CREATE TABLE `t_group_unique`(
    `id`  bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `gid` varchar(32) DEFAULT NULL COMMENT '分组标识',
    PRIMARY KEY (`id`),
    UNIQUE KEY `idx_unique_gid` (`gid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
