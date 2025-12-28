package com.festora.orderservice.dto;

import com.festora.orderservice.model.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    private Long restaurantId;
    private String sessionId;
    private String tableNumber;
    private List<OrderItem> items;
}

