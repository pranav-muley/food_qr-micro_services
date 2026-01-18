package com.festora.inventoryservice.service;

import com.festora.inventoryservice.dto.event.InventoryReservationEvent;
import com.festora.inventoryservice.dto.InventoryReserveRequest;
import com.festora.inventoryservice.dto.ReservedItemRequest;
import com.festora.inventoryservice.dto.event.OrderCancelledEvent;
import com.festora.inventoryservice.entity.InventoryReservation;
import com.festora.inventoryservice.entity.InventoryReservationItem;
import com.festora.inventoryservice.entity.InventoryStock;
import com.festora.inventoryservice.enums.ReservationStatus;
import com.festora.inventoryservice.exception.OutOfStockException;
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

    public InventoryReservationEvent tempReserve(InventoryReserveRequest request) throws Exception {
        if (request == null) throw new Exception("Request is empty");
        if (request.getItems() == null || request.getItems().isEmpty())
            throw new Exception("Items are empty");

        String orderId = request.getOrderId();
        if (orderId == null) throw new Exception("orderId missing");

        // Idempotency: return existing reservation instead of throwing
        InventoryReservation existing = reservationRepo.findByOrderId(orderId);
        if (existing != null) {
            return InventoryReservationEvent.builder()
                    .orderId(orderId)
                    .reservationId(existing.getReservationId())
                    .restaurantId(request.getRestaurantId())
                    .status(existing.getStatus().name())
                    .expiresAt(existing.getExpiresAt())
                    .build();
        }

        long now = System.currentTimeMillis();
        long ttlSeconds = request.getTtlSeconds() == 0 ? 300 : request.getTtlSeconds();
        long expiresAt = now + ttlSeconds * 1000L;

        String reservationId = UUID.randomUUID().toString();

        // Creating reservation first
        InventoryReservation reservation = new InventoryReservation();
        reservation.setReservationId(reservationId);
        reservation.setOrderId(orderId);
        reservation.setStatus(ReservationStatus.TEMP_RESERVED);
        reservation.setCreatedAt(now);
        reservation.setExpiresAt(expiresAt);
        reservationRepo.save(reservation);

        for (ReservedItemRequest item : request.getItems()) {
            if (item.getQuantity() <= 0) throw new Exception("Invalid quantity");

            String stockId = buildStockId(request.getRestaurantId(), item.getMenuItemId(), item.getVariantId());

            InventoryStock stock = stockRepo.lockById(stockId);
            if (stock == null) throw new OutOfStockException("STOCK_NOT_FOUND");

            if (stock.getAvailableQty() < item.getQuantity()) {
                throw new OutOfStockException("INSUFFICIENT_STOCK");
            }

            stock.setAvailableQty(stock.getAvailableQty() - item.getQuantity());
            stock.setUpdatedAt(now);
            stockRepo.save(stock);

            InventoryReservationItem ri = new InventoryReservationItem();
            ri.setReservationId(reservationId);
            ri.setMenuItemId(item.getMenuItemId());
            ri.setVariantId(item.getVariantId());
            ri.setQuantity(item.getQuantity());
            itemRepo.save(ri);
        }

        return InventoryReservationEvent.builder()
                .orderId(orderId)
                .reservationId(reservationId)
                .restaurantId(request.getRestaurantId())
                .status(ReservationStatus.TEMP_RESERVED.name())
                .expiresAt(expiresAt)
                .build();
    }

    public void confirmReservation(String orderId) {

        InventoryReservation reservation = reservationRepo.findByOrderId(orderId);

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            return;
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepo.save(reservation);
    }

    private String buildStockId(Long restaurantId, String menuItemId, String variantId) {
        return restaurantId + "_" + menuItemId + "_" + (variantId == null ? "NA" : variantId);
    }

    public void releaseByOrderId(OrderCancelledEvent event) {
        if (ObjectUtils.isEmpty(event)) return;

        String orderId = event.getOrderId();
        Long restaurantId = event.getRestaurantId();
        InventoryReservation reservation = reservationRepo.findByOrderId(orderId);
        if (reservation == null) return;

        if (reservation.getStatus() == ReservationStatus.RELEASED ||
                reservation.getStatus() == ReservationStatus.CANCELLED) {
            return;
        }

        // load items
        var items = itemRepo.findByReservationId(reservation.getReservationId());

        long now = System.currentTimeMillis();

        for (InventoryReservationItem item : items) {
            String stockId = buildStockId(restaurantId, item.getMenuItemId(), item.getVariantId());
            InventoryStock stock = stockRepo.lockById(stockId);
            if (stock != null) {
                stock.setAvailableQty(stock.getAvailableQty() + item.getQuantity());
                stock.setUpdatedAt(now);
                stockRepo.save(stock);
            }
        }

        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepo.save(reservation);
    }
}