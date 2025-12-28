package com.festora.cartservice.dto.client;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderCreateRequest {

    private Long restaurantId;
    private String sessionId;
    private List<OrderItem> items;
    private double subtotal;
}
