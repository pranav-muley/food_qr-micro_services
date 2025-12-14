package com.festora.menuservice.service;

import com.festora.menuservice.entity.MenuItem;
import com.festora.menuservice.repository.MenuItemRepo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {
    private final MenuItemRepo repo;

    public MenuItemService(MenuItemRepo repo) {
        this.repo = repo;
    }

    public List<MenuItem> getMenuByRestaurant(Long restaurantId) {
        return repo.findByRestaurantId(restaurantId);
    }

    public MenuItem addMenuItem(MenuItem menuItem) {
        return repo.save(menuItem);
    }

    public void deleteMenuItem(Long id) {
        repo.deleteById(id);
    }
}
