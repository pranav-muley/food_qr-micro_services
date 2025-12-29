package com.festora.inventoryservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class InventoryReserveRequest {

    private String orderId;
    private int ttlSeconds;
    private List<ReservedItemRequest> items;
}

