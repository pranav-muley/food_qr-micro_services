package com.festora.inventoryservice.repo;

import com.festora.inventoryservice.entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    Optional<InventoryItem> findByMenuItemIdAndVariantId(
            String menuItemId, String variantId
    );

}

