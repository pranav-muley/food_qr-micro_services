package com.festora.menuservice.controller;

import com.festora.menuservice.dto.MenuItemDto;
import com.festora.menuservice.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/menu/items")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @GetMapping
    public Page<MenuItemDto> getItemsByCategory(
            @RequestParam Long restaurantId,
            @RequestParam String categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return menuItemService.getMenuItemsByCategory(
                restaurantId, categoryId, page, size
        );
    }
}

