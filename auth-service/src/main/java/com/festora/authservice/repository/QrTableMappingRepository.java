package com.festora.authservice.repository;

import com.festora.authservice.model.QrTableMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QrTableMappingRepository
        extends JpaRepository<QrTableMapping, String> {
    QrTableMapping findByRestaurantIdAndTableNumber(Long restaurantId, Integer tableNumber);
}

