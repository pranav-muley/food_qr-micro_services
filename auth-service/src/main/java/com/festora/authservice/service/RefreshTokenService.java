package com.festora.authservice.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redis;
    private static final Duration TTL = Duration.ofDays(7);

    public RefreshTokenService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public String create(UUID userId) {
        String tokenId = UUID.randomUUID().toString();
        redis.opsForValue().set(key(tokenId), userId.toString(), TTL);
        return tokenId;
    }

    public UUID validateAndConsume(String tokenId) {
        String key = key(tokenId);
        String userId = redis.opsForValue().get(key);
        if (userId == null) {
            throw new RuntimeException("Invalid refresh token");
        }
        redis.delete(key); // one-time use
        return UUID.fromString(userId);
    }

    public void revokeAllForUser(UUID userId) {
        // optional optimization later (index by user)
    }

    private String key(String tokenId) {
        return "refresh:" + tokenId;
    }
}

