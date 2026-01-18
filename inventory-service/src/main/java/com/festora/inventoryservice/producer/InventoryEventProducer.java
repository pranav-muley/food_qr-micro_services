package com.festora.inventoryservice.producer;

import com.festora.inventoryservice.dto.event.InventoryReservationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReserved(InventoryReservationEvent response) {
        kafkaTemplate.send(
                "inventory.reservation-events",
                response.getOrderId(),
                response
        );
    }

    public void publishFailed(String orderId, InventoryReservationEvent failedEvent) {
        kafkaTemplate.send(
                "inventory.reservation-events",
                orderId,
                failedEvent
        );
    }
}