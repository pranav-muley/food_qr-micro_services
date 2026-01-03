package com.festora.authservice.customer.validator;

import com.festora.authservice.customer.dto.SessionData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

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

        assert restaurantId != null;
        return new SessionData(
                sessionId,
                Long.valueOf(restaurantId),
                table
        );
    }
}

