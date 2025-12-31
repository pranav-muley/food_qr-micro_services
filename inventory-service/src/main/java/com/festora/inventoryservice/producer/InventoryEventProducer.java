package com.festora.inventoryservice.producer;

import com.festora.inventoryservice.dto.InventoryFailedEvent;
import com.festora.inventoryservice.dto.InventoryReservationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReserved(InventoryReservationResponse response) {
        kafkaTemplate.send(
                "inventory.reserved",
                response.getOrderId(),
                response
        );
    }

    public void publishFailed(String orderId, InventoryFailedEvent failedEvent) {
        kafkaTemplate.send(
                "inventory.failed",
                orderId,
                failedEvent
        );
    }
}