package com.festora.inventoryservice.service;

import com.festora.inventoryservice.dto.InventoryReservationResponse;
import com.festora.inventoryservice.dto.InventoryReserveRequest;
import com.festora.inventoryservice.dto.ReservedItemRequest;
import com.festora.inventoryservice.entity.InventoryReservation;
import com.festora.inventoryservice.entity.InventoryReservationItem;
import com.festora.inventoryservice.entity.InventoryStock;
import com.festora.inventoryservice.enums.ReservationStatus;
import com.festora.inventoryservice.repo.InventoryReservationItemRepository;
import com.festora.inventoryservice.repo.InventoryReservationRepository;
import com.festora.inventoryservice.repo.InventoryStockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventoryStockRepository stockRepo;
    private final InventoryReservationRepository reservationRepo;
    private final InventoryReservationItemRepository itemRepo;

    public InventoryReservationResponse tempReserve(InventoryReserveRequest request) {

        String orderId = request.getOrderId();

        // Idempotency
        InventoryReservation reserved = reservationRepo.findByOrderId(orderId);
        if (!ObjectUtils.isEmpty(reserved)) {
            return null;
        }

        long now = System.currentTimeMillis();
        long expiresAt = now + request.getTtlSeconds() * 1000L;
        String reservationId = UUID.randomUUID().toString();

        // Deduct stock
        for (ReservedItemRequest item : request.getItems()) {

            String stockId = buildStockId(request.getRestaurantId(), item.getMenuItemId(), item.getVariantId());

            InventoryStock stock = stockRepo.lockById(stockId);

            if (stock.getAvailableQty() < item.getQuantity()) {
                throw new IllegalStateException("INSUFFICIENT_STOCK");
            }

            stock.setAvailableQty(stock.getAvailableQty() - item.getQuantity());
            stock.setUpdatedAt(now);
            stockRepo.save(stock);
        }

        // Create reservation header
        InventoryReservation reservation = new InventoryReservation();
        reservation.setReservationId(reservationId);
        reservation.setOrderId(orderId);
        reservation.setStatus(ReservationStatus.TEMP);
        reservation.setCreatedAt(now);
        reservation.setExpiresAt(expiresAt);

        reservationRepo.save(reservation);

        //  Create reservation items
        for (ReservedItemRequest item : request.getItems()) {
            InventoryReservationItem ri = new InventoryReservationItem();
            ri.setReservationId(reservationId);
            ri.setMenuItemId(item.getMenuItemId());
            ri.setVariantId(item.getVariantId());
            ri.setQuantity(item.getQuantity());

            itemRepo.save(ri);
        }

        return new InventoryReservationResponse(
                orderId,
                reservationId,
                ReservationStatus.TEMP,
                expiresAt
        );
    }

    public void confirmReservation(String orderId) {

        InventoryReservation reservation = reservationRepo.findByOrderId(orderId);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return;
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepo.save(reservation);
    }

    private String buildStockId(Long restaurantId , String menuItemId, String variantId) {
        return restaurantId + "_" + menuItemId + "_" + (variantId == null ? "NA" : variantId);
    }
}