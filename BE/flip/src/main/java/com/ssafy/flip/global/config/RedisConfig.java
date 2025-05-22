package com.ssafy.flip.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableRedisRepositories(basePackages = "com.ssafy.flip.domain.status.repository")
public class RedisConfig {

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    // RedisConnectionFactory 설정
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(10);  // 풀의 최대 유휴 연결 수
        poolConfig.setMaxTotal(50);  // 풀의 최대 총 연결 수
        poolConfig.setMaxWaitMillis(2000); // 최대 대기 시간

        JedisConnectionFactory factory = new JedisConnectionFactory(poolConfig);
        factory.setHostName(redisHostName);  // Redis 서버 호스트명
        factory.setPort(redisPort);  // Redis 서버 포트
        return factory;
    }

    // RedisTemplate 설정 (객체 직렬화 방식 설정)
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());  // 키 직렬화 방식
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());  // 값 직렬화 방식
        return template;
    }
}
