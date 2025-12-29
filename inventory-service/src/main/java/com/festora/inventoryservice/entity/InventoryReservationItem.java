package com.festora.inventoryservice.entity;

import com.festora.inventoryservice.dto.InventoryReservationItemId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "inventory_reservation_items")
@Data
@IdClass(InventoryReservationItemId.class)
public class InventoryReservationItem {

    @Id
    private String reservationId;

    @Id
    private String menuItemId;

    @Id
    private String variantId;

    private int quantity;
}

