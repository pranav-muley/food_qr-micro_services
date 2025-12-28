package com.festora.cartservice.dto;

import lombok.Data;

@Data
public class CheckoutRequest {
    private Long restaurantId;
    private String sessionId;
}