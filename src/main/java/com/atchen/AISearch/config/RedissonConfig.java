package com.atchen.AISearch.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: atchen
 * @CreateTime: 2024-08-20
 * @Description: 分布式锁机制
 * @Version: 1.0
 */

@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;
    @Value("${spring.data.redis.port}")
    private int redisPort;
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort);
        return Redisson.create(config);
    }
}
    