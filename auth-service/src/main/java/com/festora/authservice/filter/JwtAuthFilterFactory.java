package com.festora.authservice.filter;

import com.festora.authservice.security.JwtValidator;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final JwtValidator jwtValidator;

    public JwtAuthFilterFactory(JwtValidator jwtValidator) {
        super(Object.class);
        this.jwtValidator = jwtValidator;
    }

    @Override
    public GatewayFilter apply(Object config) {

        return (exchange, chain) -> {

            String authHeader = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange);
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = jwtValidator.validate(token).getBody();

                exchange = exchange.mutate()
                        .request(r -> r.headers(h -> {
                            h.add("X-User-Id", claims.getSubject());
                            h.add("X-User-Role", claims.get("role", String.class));
                            h.add("X-Restaurant-Id",
                                    String.valueOf(claims.get("restaurantId")));
                        }))
                        .build();

                return chain.filter(exchange);

            } catch (Exception e) {
                return unauthorized(exchange);
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
