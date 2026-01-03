package com.festora.authservice.service;

import com.festora.authservice.dto.SessionStartResponse;
import com.festora.authservice.model.QrTableMapping;
import com.festora.authservice.repository.QrTableMappingRepository;
import com.festora.authservice.utils.SessionJwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerSessionService {

    private static final long SESSION_TTL_SECONDS = 1800;

    private final QrTableMappingRepository qrRepo;
    private final StringRedisTemplate redis;
    private final SessionJwtUtil sessionJwtUtil;

    public SessionStartResponse startSession(String qrId) {

        QrTableMapping qr = qrRepo.findById(qrId)
                .filter(QrTableMapping::isActive)
                .orElseThrow(() -> new RuntimeException("Invalid QR"));

        String sessionId = UUID.randomUUID().toString();

        String redisKey = "session:" + sessionId;
        redis.opsForHash().put(redisKey, "restaurantId", qr.getRestaurantId().toString());
        redis.opsForHash().put(redisKey, "tableNumber", qr.getTableNumber());
        redis.opsForHash().put(redisKey, "qrId", qrId);

        redis.expire(redisKey, Duration.ofSeconds(SESSION_TTL_SECONDS));

        String token = sessionJwtUtil.createSessionToken(
                sessionId,
                qr.getRestaurantId(),
                qr.getTableNumber()
        );

        return new SessionStartResponse(token, SESSION_TTL_SECONDS);
    }
}