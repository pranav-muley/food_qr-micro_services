package com.festora.menuservice.mapper;

import com.festora.menuservice.dto.*;
import com.festora.menuservice.entity.Addon;
import com.festora.menuservice.entity.Category;
import com.festora.menuservice.entity.MenuItem;
import com.festora.menuservice.entity.Variant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MenuMapper {

    public CategoryMenuResponse toMenuResponse(
            Long restaurantId,
            List<Category> categories,
            Map<String, List<MenuItem>> itemsByCategory
    ) {

        List<CategoryDto> categoryDtos = categories.stream()
                .map(category -> CategoryDto.builder()
                        .categoryId(category.getId())
                        .name(category.getName())
                        .description(category.getDescription())
                        .items(toMenuItemDtos(
                                itemsByCategory.getOrDefault(category.getId(), List.of())
                        ))
                        .build()
                )
                .toList();

        return CategoryMenuResponse.builder()
                .restaurantId(restaurantId)
                .categories(categoryDtos)
                .build();
    }

    private List<MenuItemDto> toMenuItemDtos(List<MenuItem> items) {
        return items.stream()
                .filter(MenuItem::getEnabled)
                .map(this::toMenuItemDto)
                .toList();
    }

    public MenuItemDto toMenuItemDto(MenuItem item) {
        return MenuItemDto.builder()
                .menuItemId(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .veg(item.getVeg())
                .enabled(item.getEnabled())
                .basePrice(item.getBasePrice())
                .variants(toVariantDtos(item.getVariants()))
                .addons(toAddonDtos(item.getAddons()))
                .build();
    }

    private List<VariantDto> toVariantDtos(List<Variant> variants) {
        if (variants == null) return List.of();

        return variants.stream()
                .filter(Variant::getAvailable)
                .map(v -> VariantDto.builder()
                        .variantId(v.getId())
                        .label(v.getLabel())
                        .price(v.getPrice())
                        .available(v.getAvailable())
                        .build()
                )
                .toList();
    }

    private List<AddonDto> toAddonDtos(List<Addon> addons) {
        if (addons == null) return List.of();

        return addons.stream()
                .filter(Addon::getAvailable)
                .map(a -> AddonDto.builder()
                        .addonId(a.getId())
                        .name(a.getName())
                        .price(a.getPrice())
                        .available(a.getAvailable())
                        .build()
                )
                .toList();
    }
}
