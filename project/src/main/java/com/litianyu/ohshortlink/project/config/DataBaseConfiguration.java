package com.litianyu.ohshortlink.project.config;


/**
 * 数据库持久层配置类
 */

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataBaseConfiguration {

    /**
     * 分页插件
     */
    @Bean
    @ConditionalOnMissingBean // 如果容器中没有这个 bean 再注入
    public MybatisPlusInterceptor mybatisPlusInterceptor() { // mybatis plus 分页插件
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
