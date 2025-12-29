package com.festora.inventoryservice.entity;

import com.festora.inventoryservice.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservation")
@Data
public class InventoryReservation {

    @Id
    private String reservationId;

    @Column(unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private long expiresAt;
    private long createdAt;
}
