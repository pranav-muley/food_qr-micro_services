package com.festora.inventoryservice.consumer;

import com.festora.inventoryservice.dto.event.InventoryReservationEvent;
import com.festora.inventoryservice.dto.InventoryReserveRequest;
import com.festora.inventoryservice.dto.event.OrderCancelledEvent;
import com.festora.inventoryservice.exception.OutOfStockException;
import com.festora.inventoryservice.producer.InventoryEventProducer;
import com.festora.inventoryservice.repo.InventoryReservationRepository;
import com.festora.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final InventoryService inventoryService;
    private final InventoryEventProducer eventProducer;
    private final InventoryReservationRepository reservationRepository;

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    public void handleOrderCreated(InventoryReserveRequest event) {
        try {
            InventoryReservationEvent response = inventoryService.tempReserve(event);
            eventProducer.publishReserved(response);

        } catch (OutOfStockException ex) {
            log.warn("Out of stock for order {}", event.getOrderId());

            InventoryReservationEvent failedEvent = InventoryReservationEvent.builder()
                    .orderId(event.getOrderId())
                    .restaurantId(event.getRestaurantId())
                    .reason("OUT_OF_STOCK")
                    .build();

            eventProducer.publishFailed(event.getOrderId(), failedEvent);

        } catch (Exception ex) {
            log.error("Unexpected error for order {}", event.getOrderId(), ex);

            InventoryReservationEvent failedEvent = InventoryReservationEvent.builder()
                    .orderId(event.getOrderId())
                    .restaurantId(event.getRestaurantId())
                    .reason("INVENTORY_ERROR")
                    .build();

            eventProducer.publishFailed(event.getOrderId(), failedEvent);
        }
    }

    @KafkaListener(topics = "order.cancelled", groupId = "inventory-group")
    public void onOrderCancelled(OrderCancelledEvent event) {
        if (ObjectUtils.isEmpty(event)) {
            System.out.println("Order cancelled Event is empty");
            return;
        }
        log.info("Order cancelled, releasing inventory for order {}", event.getOrderId());
        inventoryService.releaseByOrderId(event);
    }
}
