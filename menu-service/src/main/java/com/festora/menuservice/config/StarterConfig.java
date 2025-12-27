package com.festora.menuservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.festora.menuservice.entity.Category;
import com.festora.menuservice.entity.MenuItem;
import com.festora.menuservice.repository.CategoryRepo;
import com.festora.menuservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StarterConfig implements CommandLineRunner {

    private final MenuItemRepository menuRepo;
    private final CategoryRepo categoryRepo;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {

        if (menuRepo.count() > 0 && categoryRepo.count() > 0) {
            System.out.println("✔ Menu data already exists. Skipping seed.");
            return;
        }

        System.out.println("⏳ Loading menu data...");

        InputStream menuStream =
                getClass().getResourceAsStream("/static/menu-item.json");
        InputStream categoryStream =
                getClass().getResourceAsStream("/static/categories.json");

        List<MenuItem> items = objectMapper.readValue(
                menuStream,
                new TypeReference<List<MenuItem>>() {}
        );

        List<Category> category = objectMapper.readValue(
                categoryStream,
                new TypeReference<List<Category>>() {}
        );

        menuRepo.saveAll(items);
        categoryRepo.saveAll(category);

        System.out.println("🎉 Inserted " + items.size() + " menu items.");
        System.out.println("🎉 Inserted " + category.size() + " category items.");
    }
}

