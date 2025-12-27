package com.festora.menuservice.service;

import com.festora.menuservice.dto.CategoryDto;
import com.festora.menuservice.repository.CategoryRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepo categoryRepo;

    @Cacheable(value = "categoryCache", key = "'categories:' + #restaurantId")
    public List<CategoryDto> getCategories(Long restaurantId) {
        if (restaurantId == null) {
            return Collections.emptyList();
        }
        return categoryRepo.findByRestaurantId(restaurantId)
                .stream()
                .map(cat -> CategoryDto.builder()
                        .categoryId(cat.getId())
                        .name(cat.getName())
                        .description(cat.getDescription())
                        .items(Collections.emptyList())
                        .build())
                .toList();
    }
}