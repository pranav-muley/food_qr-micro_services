package com.festora.authservice.service;

import com.festora.authservice.dto.OpenSessionRequest;
import com.festora.authservice.dto.SessionResult;
import com.festora.authservice.repository.RedisSessionRepository;
import com.festora.authservice.utils.HsJwtUtil;
import com.festora.authservice.utils.QrValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class QrSessionService {

    private final QrValidator qrValidator;
    private final RedisSessionRepository repo;
    private final long sessionTtlSeconds;
    private final HsJwtUtil hsJwtUtil;

    public QrSessionService(QrValidator qrValidator,
                            RedisSessionRepository repo,
                            @Value("${app.session.ttl-seconds}") long sessionTtlSeconds, HsJwtUtil hsJwtUtil) {
        this.qrValidator = qrValidator;
        this.repo = repo;
        this.sessionTtlSeconds = sessionTtlSeconds;
        this.hsJwtUtil = hsJwtUtil;
    }

    public SessionResult openSession(OpenSessionRequest request) {
        if(request == null) {
            return null;
        }
        String qrToken = request.getQrToken();
        // validate QR token signature + expiry
        Jws<Claims> jws = qrValidator.validateQrToken(qrToken);
        Claims c = jws.getBody();

        // expected claims in QR: restaurantId, tableNumber (number)
        String restaurantId = c.get("restaurantId", String.class);
        Integer tableNumber = null;
        Object t = c.get("tableNumber");
        if (t instanceof Integer) tableNumber = (Integer) t;
        else if (t != null) tableNumber = Integer.parseInt(t.toString());

        // create session id
        String sessionId = "sid-" + UUID.randomUUID().toString();

        long now = Instant.now().toEpochMilli();
        long expiresAt = now + sessionTtlSeconds * 1000L;

        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", sessionId);
        payload.put("restaurantId", restaurantId);
        payload.put("tableNumber", tableNumber);
        payload.put("createdAt", now);
        payload.put("expiresAt", expiresAt);

        // save to redis
        repo.save(sessionId, payload, sessionTtlSeconds);

        // session token JWT
        String sessionToken = hsJwtUtil.createSessionToken(sessionId, restaurantId, tableNumber);

        return new SessionResult(sessionId, restaurantId, tableNumber, sessionToken, expiresAt);
    }

    public SessionResult getSession(String sessionId) {
        var opt = repo.find(sessionId);
        if (opt.isEmpty()) return null;
        Map<String,Object> m = opt.get();
        return new SessionResult(
                (String) m.get("sessionId"),
                (String) m.get("restaurantId"),
                m.get("tableNumber") != null ? Integer.parseInt(m.get("tableNumber").toString()) : null,
                null,
                Long.parseLong(m.get("expiresAt").toString())
        );
    }
}
