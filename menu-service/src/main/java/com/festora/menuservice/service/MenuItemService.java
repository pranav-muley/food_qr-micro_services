package com.festora.menuservice.service;

import com.festora.menuservice.dto.MenuItemDto;
import com.festora.menuservice.dto.MenuItemPageResponse;
import com.festora.menuservice.entity.MenuItem;
import com.festora.menuservice.mapper.MenuMapper;
import com.festora.menuservice.repository.MenuItemRepository;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepo;
    private final MenuMapper menuMapper;

    @Cacheable(
            value = "menuCache",
            key = "'menu:' + #restaurantId + ':cat:' + #categoryId"
    )
    public List<MenuItemDto> getMenuItemsByCategory(
            Long restaurantId,
            String categoryId
    ) {
        return itemRepo
                .findByRestaurantIdAndCategoryId(restaurantId, categoryId)
                .stream()
                .map(menuMapper::toMenuItemDto)
                .toList();
    }

    public MenuItemPageResponse getMenuItemsResponse(
            Long restaurantId,
            String categoryId
    ) {
        if (StringUtils.isBlank(categoryId) || ObjectUtils.isEmpty(restaurantId)) {
            throw new IllegalArgumentException("categoryId and restaurantId cannot be empty");
        }
        List<MenuItemDto> items = getMenuItemsByCategory(restaurantId, categoryId);

        return MenuItemPageResponse.builder()
                .items(items)
                .totalElements(items.size())
                .build();
    }

    @CacheEvict(value = "menuCache", allEntries = true)
    public MenuItemDto createMenuItem(
            MenuItemDto dto,
            Long restaurantId,
            String categoryId
    ) {
        MenuItem entity = menuMapper.toMenuItemEntity(dto);

        entity.setRestaurantId(restaurantId);
        entity.setCategoryId(categoryId);
        entity.setEnabled(true);
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setUpdatedAt(System.currentTimeMillis());

        MenuItem saved = itemRepo.save(entity);
        return menuMapper.toMenuItemDto(saved);
    }

    /**
     * UPDATE menu item
     */
    @CacheEvict(value = "menuCache", allEntries = true)
    public MenuItemDto updateMenuItem(
            String menuItemId,
            MenuItemDto dto
    ) {
        MenuItem existing = itemRepo.findById(menuItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found: " + menuItemId)
                );

        menuMapper.updateMenuItem(existing, dto);
        existing.setUpdatedAt(System.currentTimeMillis());

        MenuItem saved = itemRepo.save(existing);
        return menuMapper.toMenuItemDto(saved);
    }

    /**
     * ENABLE / DISABLE menu item (soft delete)
     */
    @CacheEvict(value = "menuCache", allEntries = true)
    public void toggleMenuItem(String menuItemId, boolean enabled) {
        MenuItem item = itemRepo.findById(menuItemId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Menu item not found: " + menuItemId)
                );

        item.setEnabled(enabled);
        item.setUpdatedAt(System.currentTimeMillis());
        itemRepo.save(item);
    }
}