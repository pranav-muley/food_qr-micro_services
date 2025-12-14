package com.festora.authservice.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitingConfig {
    @Bean
    public Bucket bucket() {
        Refill refill = Refill.intervally(20, Duration.ofMinutes(10));
        Bandwidth limit = Bandwidth.classic(5,  refill);
        return Bucket4j.builder().addLimit(limit).build();
    }
}
