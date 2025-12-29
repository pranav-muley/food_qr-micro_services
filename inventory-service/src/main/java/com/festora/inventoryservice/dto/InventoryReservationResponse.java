package com.festora.inventoryservice.dto;

import com.festora.inventoryservice.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class InventoryReservationResponse {

    private String orderId;
    private String reservationId;
    private ReservationStatus status;
    private long expiresAt;
}

