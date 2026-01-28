package com.festora.authservice.controller;

import com.festora.authservice.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/table")
    public ResponseEntity<String> generateQRUrl(HttpServletRequest request, Integer tableNumber) {
        try {
            Long restaurantId = (Long) request.getAttribute("restaurantId");
            String generatedUrl = ownerService.getMappingRestaurantAndTable(restaurantId, tableNumber);
            return ResponseEntity.ok(generatedUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
