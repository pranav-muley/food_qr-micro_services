package com.festora.inventoryservice.repo;

import com.festora.inventoryservice.dto.InventoryReservationItemId;
import com.festora.inventoryservice.entity.InventoryReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReservationItemRepository
        extends JpaRepository<InventoryReservationItem, InventoryReservationItemId> {

    List<InventoryReservationItem> findByReservationId(String reservationId);

    void deleteByReservationId(String reservationId);
}


