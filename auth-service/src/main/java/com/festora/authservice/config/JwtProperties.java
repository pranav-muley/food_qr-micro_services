package com.festora.authservice.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    @PostConstruct
    void debug() {
        System.out.println("JWT TTL = " + accessTokenTtlMinutes);
    }

    private String issuer;
    private String audience;

    private String publicKey;
    private String privateKey;

    private long accessTokenTtlMinutes;
}