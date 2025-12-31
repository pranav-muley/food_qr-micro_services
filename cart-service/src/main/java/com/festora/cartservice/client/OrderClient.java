package com.festora.cartservice.client;

import com.festora.cartservice.dto.client.OrderCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.order.base-url}")
    private String orderBaseUrl;

    public Object createOrder(OrderCreateRequest request) {

        return webClientBuilder.build()
                .post()
                .uri(orderBaseUrl + "/orders/create")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Object.class) // order response DTO later
                .block();
    }
}