package com.atchen.AISearch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类，设置序列化
 */
@Configuration
public class RedisConfig {
 
    /**
     * 设置序列化
     *
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
 
        // String类型 key序列器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // String类型 value序列器
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
 
        // Hash类型 key序列器
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // Hash类型 value序列器
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
 
        // 把连接工厂放进去
        redisTemplate.setConnectionFactory(connectionFactory);
 
        return redisTemplate;
    }
 
}