package com.festora.menuservice.service;

import com.festora.menuservice.dto.CategoryDto;
import com.festora.menuservice.dto.CategoryMenuResponse;
import com.festora.menuservice.entity.Category;
import com.festora.menuservice.mapper.MenuMapper;
import com.festora.menuservice.repository.CategoryRepo;
import com.festora.menuservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuOverviewService {

    private static final int PREVIEW_LIMIT = 5;

    private final CategoryRepo categoryRepo;
    private final MenuItemRepository itemRepo;
    private final MenuMapper menuMapper;

    @Cacheable(
            value = "menuOverviewCache",
            key = "'menu:overview:' + #restaurantId"
    )
    public CategoryMenuResponse getMenuOverview(Long restaurantId) {

        List<Category> categories =
                categoryRepo.findByRestaurantId(restaurantId);

        List<CategoryDto> categoryDtos =
                categories.stream()
                        .map(category -> CategoryDto.builder()
                                .categoryId(category.getId())
                                .name(category.getName())
                                .description(category.getDescription())
                                .items(
                                        loadPreviewItems(
                                                restaurantId,
                                                category.getId()
                                        )
                                )
                                .build()
                        )
                        .toList();

        return CategoryMenuResponse.builder()
                .restaurantId(restaurantId)
                .categories(categoryDtos)
                .build();
    }

    private List<com.festora.menuservice.dto.MenuItemDto> loadPreviewItems(
            Long restaurantId,
            String categoryId
    ) {
        return itemRepo
                .findByRestaurantIdAndCategoryId(
                        restaurantId,
                        categoryId,
                        PageRequest.of(
                                0,
                                PREVIEW_LIMIT,
                                Sort.by("name").ascending()
                        )
                )
                .map(menuMapper::toMenuItemDto)
                .getContent();
    }
}