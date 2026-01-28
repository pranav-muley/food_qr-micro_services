package com.festora.authservice.controller;

import com.festora.authservice.service.OwnerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/qr")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;

    @GetMapping("/table")
    public ResponseEntity<?> generateQRUrl(
            HttpServletRequest request,
            @RequestParam Integer tableNumber
    ) {
        try {
            Long restaurantId = (Long) request.getAttribute("restaurantId");

            if (restaurantId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            String generatedUrl = ownerService.getMappingRestaurantAndTable(restaurantId, tableNumber);
            System.out.println(generatedUrl);
            return ResponseEntity.ok(generatedUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
