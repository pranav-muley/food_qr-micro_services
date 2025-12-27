package com.festora.menuservice.repository;

import com.festora.menuservice.entity.Category;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CategoryRepo extends MongoRepository<Category, String> {

    List<Category> findByRestaurantId(Long restaurantId);
}


