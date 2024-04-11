package com.litianyu.ohshortlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // 注解
@MapperScan("com.litianyu.ohshortlink.admin.dao.mapper") // 这里最好能够具体到某个包路径，会减少很多无用的扫描流程
public class AdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
