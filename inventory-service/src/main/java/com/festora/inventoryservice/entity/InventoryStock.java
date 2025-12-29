package com.festora.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory_stock")
@Data
public class InventoryStock {

    @Id
    private String id; // menuItemId|variantId

    private String menuItemId;
    private String variantId;

    private int availableQty;
    private long updatedAt;
}
