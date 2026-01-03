package com.festora.authservice.customer.validator;

import com.festora.authservice.customer.dto.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class SessionStore {

    private final StringRedisTemplate redis;

    public SessionData get(String sessionId) {

        String key = "session:" + sessionId;

        if (!Boolean.TRUE.equals(redis.hasKey(key))) {
            return null;
        }

        String restaurantId = (String) redis.opsForHash().get(key, "restaurantId");
        String table = (String) redis.opsForHash().get(key, "tableNumber");

        return new SessionData(
                sessionId,
                Long.valueOf(restaurantId),
                table
        );
    }
}

