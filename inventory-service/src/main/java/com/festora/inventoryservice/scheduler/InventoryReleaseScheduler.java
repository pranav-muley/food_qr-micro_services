package com.festora.inventoryservice.scheduler;

import com.festora.inventoryservice.entity.StockReservation;
import com.festora.inventoryservice.enums.ReservationStatus;
import com.festora.inventoryservice.repo.ReservationRepository;
import com.festora.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class InventoryReleaseScheduler {

    private final ReservationRepository reservationRepository;
    private final InventoryService inventoryService;

    @Scheduled(fixedDelay = 60000) // every 1 min
    public void releaseExpiredReservations() {

        LocalDateTime expiry = LocalDateTime.now().minusMinutes(10);

        List<StockReservation> expired =
                reservationRepository.findByStatusAndReservedAtBefore(
                        ReservationStatus.RESERVED,
                        expiry
                );

        expired.forEach(inventoryService::releaseStock);
    }
}
