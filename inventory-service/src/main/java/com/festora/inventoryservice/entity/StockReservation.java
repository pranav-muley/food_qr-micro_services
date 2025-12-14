package com.festora.inventoryservice.entity;

import com.festora.inventoryservice.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(
        name = "stock_reservation",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"order_id", "menu_item_id", "variant_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String menuItemId;
    private String variantId;

    private int quantity;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    private LocalDateTime reservedAt;
}

