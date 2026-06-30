package com.parttime.common.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Common模块自动配置
 * 通过spring.factories自动加载。
 * ComponentScan始终激活，MapperScan仅在存在DataSource bean时激活。
 */
@Configuration
@ComponentScan("com.parttime.common")
public class CommonAutoConfiguration {

    @Configuration
    @ConditionalOnBean(DataSource.class)
    @MapperScan("com.parttime.common.mapper")
    static class CommonMybatisConfiguration {
    }
}
