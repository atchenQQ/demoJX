package com.atchen.AISearch.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/*
 * @description 分页拦截器
 * @author atchen
 * @date 2024/8/20
 */

@Configuration
public class PageConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 将 MP 里面的分页插件设置 MP
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}