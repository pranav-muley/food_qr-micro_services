package com.festora.inventoryservice.controller;

import com.festora.inventoryservice.dto.InventoryReservationResponse;
import com.festora.inventoryservice.dto.InventoryReserveRequest;
import com.festora.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ==============================
    // TEMP RESERVE
    // ==============================/inventory/temp-reserve
    @PostMapping("/temp-reserve")
    public ResponseEntity<InventoryReservationResponse> tempReserve(
            @RequestBody InventoryReserveRequest request
    ) {
        InventoryReservationResponse response = inventoryService.tempReserve(request);
        return ResponseEntity.ok(response);
    }

    // ==============================
    // CONFIRM AFTER PAYMENT
    // ==============================
    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirm(@PathVariable String orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok().build();
    }
}