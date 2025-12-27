package com.festora.menuservice.service;

import com.festora.menuservice.entity.Category;
import com.festora.menuservice.entity.MenuItem;
import com.festora.menuservice.repository.CategoryRepo;
import com.festora.menuservice.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final CategoryRepo categoryRepo;
    private final MenuItemRepository itemRepo;

    public List<Category> getMenuByRestaurant(Long restaurantId) {

        List<Category> categories =
                categoryRepo.findByRestaurantId(restaurantId);

        for (Category c : categories) {
            List<MenuItem> items =
                    itemRepo.findByCategoryId(c.getId());
            c.setItems(items);
        }

        return categories;
    }
}

