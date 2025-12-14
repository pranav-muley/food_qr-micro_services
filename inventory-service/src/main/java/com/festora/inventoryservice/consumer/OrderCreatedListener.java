package com.festora.inventoryservice.consumer;

import com.festora.inventoryservice.dto.OrderCreatedEvent;
import com.festora.inventoryservice.entity.StockReservation;
import com.festora.inventoryservice.enums.ReservationStatus;
import com.festora.inventoryservice.exception.OutOfStockException;
import com.festora.inventoryservice.producer.InventoryEventProducer;
import com.festora.inventoryservice.repo.ReservationRepository;
import com.festora.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderCreatedListener {

    private final InventoryService inventoryService;
    private final InventoryEventProducer eventProducer;
    private final ReservationRepository reservationRepository;

    @KafkaListener(topics = "order.created", groupId = "inventory-group")
    public void handleOrderCreated(OrderCreatedEvent event) {

        try {
            inventoryService.reserveStock(
                    event.getOrderId(),
                    event.getItems()
            );

            eventProducer.publishReserved(event.getOrderId());

        } catch (OutOfStockException ex) {

            log.warn("Out of stock for order {}", event.getOrderId());
            eventProducer.publishFailed(event.getOrderId(), "OUT_OF_STOCK");

        } catch (Exception ex) {

            log.error("Unexpected error", ex);
            throw ex; // Kafka retry
        }
    }

    @KafkaListener(topics = "order.cancelled")
    public void onOrderCancelled(String orderId) {

        List<StockReservation> reservations =
                reservationRepository.findByOrderIdAndStatus(
                        orderId,
                        ReservationStatus.RESERVED
                );

        reservations.forEach(res -> {
            inventoryService.releaseStock(res);
        });
    }
}
