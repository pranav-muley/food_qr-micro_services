package com.festora.inventoryservice.controller;

import com.festora.inventoryservice.dto.event.InventoryReservationEvent;
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

    @PostMapping("/temp-reserve")
    public ResponseEntity<InventoryReservationEvent> tempReserve(
            @RequestBody InventoryReserveRequest request
    ) {
        try {
            InventoryReservationEvent response = inventoryService.tempReserve(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirm(@PathVariable String orderId) {
        inventoryService.confirmReservation(orderId);
        return ResponseEntity.ok().build();
    }
}