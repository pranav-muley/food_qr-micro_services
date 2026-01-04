package com.festora.authservice.utils;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisHealthCheck {
    private final RedisConnectionFactory factory;

    @Value("${spring.data.redis.url}")
    private String redisUrl;

    public RedisHealthCheck(RedisConnectionFactory factory) {
        this.factory = factory;
    }

    @PostConstruct
    public void checkRedis() {
        log.info("Using Redis URL = {}", redisUrl);

        try {
            String pong = factory.getConnection().ping();
            log.info("Redis PING response: {}", pong);
        } catch (Exception e) {
            log.error("❌ Redis connection failed", e);
        }
    }
}

