package com.festora.inventoryservice.consumer;

import com.festora.inventoryservice.dto.InventoryFailedEvent;
import com.festora.inventoryservice.dto.InventoryReservationResponse;
import com.festora.inventoryservice.dto.InventoryReserveRequest;
import com.festora.inventoryservice.exception.OutOfStockException;
import com.festora.inventoryservice.producer.InventoryEventProducer;
import com.festora.inventoryservice.repo.InventoryReservationRepository;
import com.festora.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

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
            InventoryReservationResponse response = inventoryService.tempReserve(event);
            eventProducer.publishReserved(response);

        } catch (OutOfStockException ex) {
            log.warn("Out of stock for order {}", event.getOrderId());
            InventoryFailedEvent failedEvent = InventoryFailedEvent.builder()
                    .orderId(event.getOrderId())
                    .restaurantId(event.getRestaurantId())
                    .reason("OUT_OF_STOCK")
                    .build();
            eventProducer.publishFailed(event.getOrderId(), failedEvent);
        } catch (Exception ex) {
            log.error("Unexpected error", ex);
            throw ex; // Kafka retry
        }
    }

    @KafkaListener(topics = "order.cancelled")
    public void onOrderCancelled(String orderId) {

//        List<InventoryReservation> reservations =
//                reservationRepository.findByOrderIdAndStatus(
//                        orderId,
//                        ReservationStatus.RESERVED
//                );
//
//        reservations.forEach(res -> {
//            inventoryService.releaseStock(res);
//        });
    }
}
