package com.festora.authservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.stereotype.Component;

import java.security.PublicKey;

@Component
public class QrValidator {

    private final HsJwtUtil jwtUtil;

    public QrValidator(HsJwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public Jws<Claims> validateQrToken(String token) {
        return jwtUtil.parseAndValidateQr(token);
    }
}