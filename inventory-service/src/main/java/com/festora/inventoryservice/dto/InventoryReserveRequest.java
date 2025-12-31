package com.festora.inventoryservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryReserveRequest {

    private String orderId;
    private Long restaurantId;
    private int ttlSeconds;
    private List<ReservedItemRequest> items;
}

