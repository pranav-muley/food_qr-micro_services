package com.festora.menuservice.controller;

import com.festora.menuservice.dto.CategoryDto;
import com.festora.menuservice.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/get")
    public List<CategoryDto> getCategories(
            @RequestParam Long restaurantId
    ) {
        return categoryService.getCategories(restaurantId);
    }
}
