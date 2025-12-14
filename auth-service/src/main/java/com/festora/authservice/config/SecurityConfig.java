package com.festora.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // Disable CSRF for stateless JWT endpoints
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/v1/session/**")
                        .disable()
                )

                // Stateless API (very important for JWT)
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/session/open").permitAll()
                        .requestMatchers("/v1/session/validate").permitAll() // if you have this
                        .requestMatchers("/public/**").permitAll()
                        .anyRequest().authenticated()
                )

                // No OAuth2 Resource Server (unless you're using JWKs)
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}