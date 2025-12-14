package com.festora.inventoryservice.repo;

import com.festora.inventoryservice.entity.StockReservation;
import com.festora.inventoryservice.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<StockReservation, Long> {

    boolean existsByOrderIdAndMenuItemIdAndVariantId(
            String orderId,
            String menuItemId,
            String variantId
    );

    List<StockReservation> findByOrderIdAndStatus(
            String orderId,
            ReservationStatus status
    );

    List<StockReservation> findByStatusAndReservedAtBefore(ReservationStatus reservationStatus, LocalDateTime expiry);

}


