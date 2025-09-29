package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;


@Configuration
public class RedisConfig {
    /**
     * 配置Redis模板
     * 設置Redis的序列化方式，確保鍵值對可以正確存儲和檢索
     * 使用字符串序列化器確保鍵的可讀性，支援模式匹配查詢
     * 
     * @param factory Redis連接工廠
     * @return RedisTemplate 配置好的Redis模板
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        // 使用可讀性的序列化，確保 keys 可被模式匹配到（click:* / shorturl:*）
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setHashValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.afterPropertiesSet();
        return template;
    }
}