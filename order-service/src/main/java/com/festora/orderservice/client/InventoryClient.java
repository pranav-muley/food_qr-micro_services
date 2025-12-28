package com.festora.orderservice.client;

import com.festora.orderservice.model.Order;

public interface InventoryClient {

    void tempReserve(Order order);

    void confirm(String orderId);
}

