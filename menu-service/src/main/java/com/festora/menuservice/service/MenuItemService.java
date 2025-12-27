package com.festora.menuservice.service;

import com.festora.menuservice.dto.MenuItemDto;
import com.festora.menuservice.mapper.MenuMapper;
import com.festora.menuservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository itemRepo;
    private final MenuMapper menuMapper;

    /**
     * OPTION A: Lazy load menu items by category
     */
    @Cacheable(
            value = "menuCache",
            key = "'menu:' + #restaurantId + ':cat:' + #categoryId + ':p:' + #page"
    )
    public Page<MenuItemDto> getMenuItemsByCategory(
            Long restaurantId,
            String categoryId,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("name").ascending()   // stable pagination
        );

        return itemRepo
                .findByRestaurantIdAndCategoryId(
                        restaurantId,
                        categoryId,
                        pageable
                )
                .map(menuMapper::toMenuItemDto);
    }

    /**
     * Evict ALL menu cache entries.
     * Called via event (menu update, price change, availability change)
     */
    @CacheEvict(value = "menuCache", allEntries = true)
    public void refreshMenu(Long restaurantId) {
        // intentionally empty
        // eviction handled by annotation
    }
}
