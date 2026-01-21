package com.festora.inventoryservice.controller;

import com.festora.inventoryservice.dto.event.InventoryReservationEvent;
import com.festora.inventoryservice.dto.InventoryReserveRequest;
import com.festora.inventoryservice.enums.ReservationStatus;
import com.festora.inventoryservice.exception.OutOfStockException;
import com.festora.inventoryservice.producer.InventoryEventProducer;
import com.festora.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;

    @PostMapping("/temp-reserve")
    public ResponseEntity<InventoryReservationEvent> tempReserve(
            @RequestBody InventoryReserveRequest request
    ) {
        InventoryReservationEvent event = new InventoryReservationEvent();
        try {
            event = inventoryService.tempReserve(request);
            inventoryEventProducer.publishReserved(event);
            return ResponseEntity.ok(event);
        } catch (OutOfStockException ex) {
            log.warn("Out of stock for order {}", event.getOrderId());

            InventoryReservationEvent failedEvent = InventoryReservationEvent.builder()
                    .orderId(event.getOrderId())
                    .restaurantId(event.getRestaurantId())
                    .reason("OUT_OF_STOCK")
                    .status(ReservationStatus.CANCELLED.name())
                    .build();

            inventoryEventProducer.publishFailed(event.getOrderId(), failedEvent);

        } catch (Exception ex) {
            log.error("Unexpected error for order {} ", ex.getMessage());

            InventoryReservationEvent failedEvent = InventoryReservationEvent.builder()
                    .orderId(event.getOrderId())
                    .restaurantId(event.getRestaurantId())
                    .reason("INVENTORY_ERROR")
                    .status(ReservationStatus.CANCELLED.name())
                    .build();

            inventoryEventProducer.publishFailed(event.getOrderId(), failedEvent);
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirm(@PathVariable String orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok().build();
    }
}