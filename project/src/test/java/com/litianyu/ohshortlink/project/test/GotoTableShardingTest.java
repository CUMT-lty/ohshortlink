package com.litianyu.ohshortlink.project.test;

public class GotoTableShardingTest {

    public static final String SQL = "CREATE TABLE `t_link_goto_%d`\n" +
            "(\n" +
            "    `id`             bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',\n" +
            "    `gid`            varchar(32)  DEFAULT 'default' COMMENT '分组标识',\n" +
            "    `full_short_url` varchar(128) DEFAULT NULL COMMENT '完整短链接',\n" +
            "    PRIMARY KEY (`id`),\n" +
            "    UNIQUE KEY `idx_full_short_url` (`full_short_url`) USING BTREE\n" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n";

    public static void main(String[] args) {
        for (int i = 0; i < 16; i++) {
            System.out.printf(SQL, i);
        }
    }
}
