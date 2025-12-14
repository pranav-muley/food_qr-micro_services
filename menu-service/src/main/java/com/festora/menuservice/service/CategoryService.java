package com.festora.menuservice.service;

import com.festora.menuservice.entity.Category;
import com.festora.menuservice.repository.CategoryRepo;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo repo;

    public CategoryService(CategoryRepo repo) {
        this.repo = repo;
    }

    public List<Category> getAllCategories() {
        return repo.findAll();
    }

    public Category addCategory(Category category) {
        return repo.save(category);
    }

    public void deleteCategory(Long id) {
        repo.deleteById(id);
    }
}

