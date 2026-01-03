package com.festora.authservice.service;

import com.festora.authservice.dto.SessionStartResponse;
import com.festora.authservice.model.QrTableMapping;
import com.festora.authservice.repository.QrTableMappingRepository;
import com.festora.authservice.utils.SessionJwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerSessionService {

    private static final long SESSION_TTL_SECONDS = 1800;

    private final QrTableMappingRepository qrRepo;
    private final StringRedisTemplate redis;
    private final SessionJwtUtil sessionJwtUtil;

    public SessionStartResponse startSession(String qrId, HttpServletRequest request) {

        final String deviceId = request.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalArgumentException("Missing X-Device-Id header");
        }

        final QrTableMapping qr = qrRepo.findById(qrId)
                .filter(QrTableMapping::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Invalid QR"));

        final long ttl = SESSION_TTL_SECONDS;

        final String linkKey = "QR-SESSION: " + deviceId;

        String sessionId = redis.opsForValue().get(linkKey);

        if (sessionId != null) {

            final String sessionKey = "session:" + sessionId;

            if (Boolean.TRUE.equals(redis.hasKey(sessionKey))) {
                Duration expiry = Duration.ofSeconds(ttl);
                redis.expire(linkKey, expiry);
                redis.expire(sessionKey, expiry);
                Long remainingTtl = redis.getExpire(sessionKey);
                return new SessionStartResponse(createToken(sessionId, qr), remainingTtl > 0 ? remainingTtl : ttl);
            }
        }

        // ---- Create NEW session ----
        sessionId = UUID.randomUUID().toString();
        final String sessionKey = "session:" + sessionId;

        Map<String, String> sessionData = Map.of(
                "restaurantId", qr.getRestaurantId().toString(),
                "tableNumber", qr.getTableNumber(),
                "deviceId", deviceId
        );

        redis.opsForHash().putAll(sessionKey, sessionData);
        redis.expire(sessionKey, Duration.ofSeconds(ttl));

        redis.opsForValue().set(linkKey, sessionId, Duration.ofSeconds(ttl));

        return new SessionStartResponse(createToken(sessionId, qr), ttl);
    }

    private String createToken(String sessionId, QrTableMapping qr) {
        return sessionJwtUtil.createSessionToken(
                sessionId,
                qr.getRestaurantId(),
                qr.getTableNumber()
        );
    }
}