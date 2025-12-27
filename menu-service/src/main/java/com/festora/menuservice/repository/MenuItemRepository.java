package com.festora.menuservice.repository;

import com.festora.menuservice.entity.MenuItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByCategoryId(String categoryId);
}


