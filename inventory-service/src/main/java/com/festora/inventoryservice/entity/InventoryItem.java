package com.festora.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "inventory_item",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"menu_item_id", "variant_id"}
        )
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String menuItemId;
    private String variantId;

    private int totalQuantity;
    private int availableQuantity;
    private int reservedQuantity;

    @Version
    private Long version;
}


