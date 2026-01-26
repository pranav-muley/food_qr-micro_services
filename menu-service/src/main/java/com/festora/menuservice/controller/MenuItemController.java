package com.festora.menuservice.controller;

import com.festora.menuservice.dto.MenuItemDto;
import com.festora.menuservice.dto.MenuItemPageResponse;
import com.festora.menuservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping("/items")
    public ResponseEntity<MenuItemPageResponse> getItemsByCategory(
            @RequestParam Long restaurantId,
            @RequestParam String categoryId
    ) {
        try {
            MenuItemPageResponse response = menuItemService.getMenuItemsResponse(restaurantId, categoryId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/items")
    public ResponseEntity<MenuItemDto> create(
            @RequestParam Long restaurantId,
            @RequestParam String categoryId,
            @RequestBody MenuItemDto dto
    ) {
        return ResponseEntity.ok(
                menuItemService.createMenuItem(dto, restaurantId, categoryId)
        );
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItemDto> update(
            @PathVariable String id,
            @RequestBody MenuItemDto dto
    ) {
        return ResponseEntity.ok(
                menuItemService.updateMenuItem(id, dto)
        );
    }

    @PatchMapping("/items/{id}/toggle")
    public ResponseEntity<Void> toggle(
            @PathVariable String id,
            @RequestParam boolean enabled
    ) {
        menuItemService.toggleMenuItem(id, enabled);
        return ResponseEntity.ok().build();
    }
}

