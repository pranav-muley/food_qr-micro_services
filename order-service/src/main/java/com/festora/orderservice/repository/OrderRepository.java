package com.festora.orderservice.repository;

import com.festora.orderservice.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    Order findByOrderIdAndUserId(String orderId, String userId);
}
