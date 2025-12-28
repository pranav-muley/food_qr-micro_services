package com.festora.cartservice.dto;

import lombok.Data;

@Data
public class UpdateCartItemRequest {
    private Long restaurantId;
    private String sessionId;
    private int quantity;
}