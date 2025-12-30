package com.festora.orderservice.client;

import com.festora.orderservice.model.Order;
import com.festora.orderservice.model.OrderItem;

import java.util.List;

public interface InventoryClient {

    void tempReserve(Order order);
    void tempReserve(String orderId, List<OrderItem> item);
    void confirm(String orderId);
}

