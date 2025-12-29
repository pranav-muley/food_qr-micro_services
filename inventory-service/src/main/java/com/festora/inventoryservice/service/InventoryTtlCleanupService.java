package com.festora.inventoryservice.service;

import com.festora.inventoryservice.entity.InventoryReservation;
import com.festora.inventoryservice.entity.InventoryReservationItem;
import com.festora.inventoryservice.entity.InventoryStock;
import com.festora.inventoryservice.repo.InventoryReservationItemRepository;
import com.festora.inventoryservice.repo.InventoryReservationRepository;
import com.festora.inventoryservice.repo.InventoryStockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryTtlCleanupService {

    private final InventoryReservationRepository reservationRepo;
    private final InventoryReservationItemRepository reservationItemRepo;
    private final InventoryStockRepository stockRepo;

    @Transactional
    public void cleanupExpiredReservations() {

        long now = System.currentTimeMillis();

        List<InventoryReservation> expiredReservations =
                reservationRepo.findExpiredTempReservations(now);

        for (InventoryReservation reservation : expiredReservations) {

            log.warn("TTL cleanup: releasing reservation {} for order {}",
                    reservation.getReservationId(),
                    reservation.getOrderId()
            );

            // Fetch reserved items
            List<InventoryReservationItem> items =
                    reservationItemRepo.findByReservationId(
                            reservation.getReservationId()
                    );

            // Restore stock
            for (InventoryReservationItem item : items) {

                String stockId = buildStockId(
                        item.getMenuItemId(),
                        item.getVariantId()
                );

                InventoryStock stock = stockRepo.lockById(stockId);

                stock.setAvailableQty(
                        stock.getAvailableQty() + item.getQuantity()
                );
                stock.setUpdatedAt(now);

                stockRepo.save(stock);
            }

            // Delete reservation items
            reservationItemRepo.deleteByReservationId(
                    reservation.getReservationId()
            );

            // Delete reservation header
            reservationRepo.delete(reservation);
        }
    }

    private String buildStockId(String menuItemId, String variantId) {
        return menuItemId + "|" + (variantId == null ? "NA" : variantId);
    }
}