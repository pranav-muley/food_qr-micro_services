package com.festora.orderservice.consumer;

import com.festora.orderservice.dto.InventoryFailedEvent;
import com.festora.orderservice.dto.InventoryReservedEvent;
import com.festora.orderservice.enums.OrderStatus;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.repository.OrderRepository;
import com.festora.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventListener {
    private final OrderService orderService;

    @KafkaListener(topics = "inventory.temp_reserved", groupId = "order-group")
    public void onTempReserved(InventoryReservedEvent event) {
        log.info("Inventory reserved for order {}", event.getOrderId());
        orderService.markInventoryReserved(event.getOrderId());
    }


    @KafkaListener(topics = "inventory.failed", groupId = "order-group")
    public void onInventoryFailed(InventoryFailedEvent event) {
        log.warn("Inventory failed for order {}", event.getOrderId());
        orderService.markInventoryFailed(event.getOrderId());
    }

}
