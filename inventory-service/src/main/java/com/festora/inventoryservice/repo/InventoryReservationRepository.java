package com.festora.inventoryservice.repo;

import com.festora.inventoryservice.entity.InventoryReservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryReservationRepository
        extends JpaRepository<InventoryReservation, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT r FROM InventoryReservation r
        WHERE r.status = 'TEMP'
          AND r.expiresAt < :now
    """)
    List<InventoryReservation> findExpiredTempReservations(
            @Param("now") long now
    );

    InventoryReservation findByOrderId(String orderId );
}




