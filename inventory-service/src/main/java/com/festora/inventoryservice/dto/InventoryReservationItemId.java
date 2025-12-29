package com.festora.inventoryservice.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class InventoryReservationItemId implements Serializable {
    private String reservationId;
    private String menuItemId;
    private String variantId;
}
