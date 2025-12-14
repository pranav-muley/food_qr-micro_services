package com.festora.inventoryservice.producer;

import com.festora.inventoryservice.dto.InventoryFailedEvent;
import com.festora.inventoryservice.dto.InventoryReservedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReserved(String orderId) {
        kafkaTemplate.send(
                "inventory.reserved",
                orderId,
                new InventoryReservedEvent(orderId)
        );
    }

    public void publishFailed(String orderId, String reason) {
        kafkaTemplate.send(
                "inventory.failed",
                orderId,
                new InventoryFailedEvent(orderId, reason)
        );
    }
}