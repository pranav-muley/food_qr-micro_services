package com.festora.authservice.config;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    private final StringRedisTemplate redis;

    public LoginRateLimiter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void validateLoginAllowed(String email) {
        String key = key(email);
        String attempts = "0";
        try {
           attempts = redis.opsForValue().get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            throw new RuntimeException("Too many login attempts. Try later.");
        }
    }

    public void onLoginFailure(String email) {
        String key = key(email);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, BLOCK_DURATION);
        }
    }

    public void onLoginSuccess(String email) {
        redis.delete(key(email));
    }

    private String key(String email) {
        return "login:fail:" + email.toLowerCase();
    }
}
