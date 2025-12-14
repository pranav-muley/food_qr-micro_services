package com.festora.authservice.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class HsJwtUtil {

    @Value("${app.qr.secret}")
    private String qrSecret; // must be set via APP_QR_SECRET

    @Value("${app.session.token-ttl-seconds:1800}")
    private int sessionTokenTtlSeconds;

    private Key qrKey;
    private Key sessionKey;

    @PostConstruct
    public void init() {
        // for HS256 it's fine to use the same secret bytes.
        // Ensure the secret has enough entropy (>= 32 bytes). Prefer >= 64.
        byte[] keyBytes = qrSecret.getBytes(StandardCharsets.UTF_8);
        qrKey = Keys.hmacShaKeyFor(keyBytes);
        sessionKey = Keys.hmacShaKeyFor(keyBytes); // optional: you can use a different env var for session signing
    }

    // ===== QR token creation (HS256) - useful for generator if you want to sign from Java =====
    public String createQrToken(Map<String, Object> claims, int ttlSeconds) {
        long now = System.currentTimeMillis();
        JwtBuilder b = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlSeconds * 1000L))
                .signWith(qrKey, SignatureAlgorithm.HS256);
        return b.compact();
    }

    // Validate incoming QR token (throws io.jsonwebtoken.JwtException on invalid)
    public Jws<Claims> parseAndValidateQr(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(qrKey)
                .build()
                .parseClaimsJws(token);
    }

    // ===== Session token creation (HS256) - you can use same key or separate secret if preferred =====
    public String createSessionToken(String sessionId, String restaurantId, Integer tableNumber) {
        long now = System.currentTimeMillis();
        JwtBuilder b = Jwts.builder()
                .setSubject(sessionId)
                .setIssuer("auth-service")
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + sessionTokenTtlSeconds * 1000L))
                .addClaims(Map.of("restaurantId", restaurantId, "tableNumber", tableNumber))
                .signWith(sessionKey, SignatureAlgorithm.HS256);
        return b.compact();
    }

    // Parse session token
    public Jws<Claims> parseAndValidateSession(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(sessionKey)
                .build()
                .parseClaimsJws(token);
    }
}
