package com.festora.inventoryservice.service;

import com.festora.inventoryservice.dto.OrderItem;
import com.festora.inventoryservice.entity.InventoryItem;
import com.festora.inventoryservice.entity.StockReservation;
import com.festora.inventoryservice.enums.ReservationStatus;
import com.festora.inventoryservice.exception.OutOfStockException;
import com.festora.inventoryservice.repo.InventoryRepository;
import com.festora.inventoryservice.repo.ReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void reserveStock(
            String orderId,
            List<OrderItem> items
    ) {

        for (OrderItem item : items) {

            // 🔒 Idempotency check
            boolean exists = reservationRepository
                    .existsByOrderIdAndMenuItemIdAndVariantId(
                            orderId,
                            item.getMenuItemId(),
                            item.getVariantId()
                    );

            if (exists) continue;

            InventoryItem inventory = inventoryRepository
                    .findByMenuItemIdAndVariantId(
                            item.getMenuItemId(),
                            item.getVariantId()
                    )
                    .orElseThrow(() -> new RuntimeException("Inventory not found"));

            if (inventory.getAvailableQuantity() < item.getQuantity()) {
                throw new OutOfStockException("OUT_OF_STOCK");
            }

            inventory.setAvailableQuantity(
                    inventory.getAvailableQuantity() - item.getQuantity()
            );
            inventory.setReservedQuantity(
                    inventory.getReservedQuantity() + item.getQuantity()
            );

            inventoryRepository.save(inventory);

            StockReservation reservation = new StockReservation(
                    null,
                    orderId,
                    item.getMenuItemId(),
                    item.getVariantId(),
                    item.getQuantity(),
                    ReservationStatus.RESERVED,
                    LocalDateTime.now()
            );

            reservationRepository.save(reservation);
        }
    }

    @Transactional
    public void releaseStock(StockReservation res) {

        InventoryItem inventory = inventoryRepository
                .findByMenuItemIdAndVariantId(
                        res.getMenuItemId(),
                        res.getVariantId()
                )
                .orElseThrow();

        inventory.setAvailableQuantity(
                inventory.getAvailableQuantity() + res.getQuantity()
        );
        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - res.getQuantity()
        );

        inventoryRepository.save(inventory);

        res.setStatus(ReservationStatus.RELEASED);
        reservationRepository.save(res);
    }
}