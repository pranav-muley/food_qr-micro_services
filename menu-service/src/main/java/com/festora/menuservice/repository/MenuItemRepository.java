package com.festora.menuservice.repository;

import com.festora.menuservice.entity.MenuItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import org.springframework.data.domain.Page;

public interface MenuItemRepository extends MongoRepository<MenuItem, String> {

    Page<MenuItem> findByRestaurantIdAndCategoryId(
            Long restaurantId,
            String categoryId,
            Pageable pageable
    );
}



