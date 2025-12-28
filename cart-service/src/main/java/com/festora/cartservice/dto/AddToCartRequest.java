package com.festora.cartservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddToCartRequest {

    private String sessionId;
    private Long restaurantId;

    private String menuItemId;

    // Optional
    private String variantId;
    private List<String> addonIds;

    private int quantity;
}
