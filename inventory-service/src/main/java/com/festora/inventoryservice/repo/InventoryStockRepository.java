package com.festora.inventoryservice.repo;

import com.festora.inventoryservice.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM InventoryStock s
        WHERE s.id = :id
    """)
    InventoryStock lockById(@Param("id") String id);
}



