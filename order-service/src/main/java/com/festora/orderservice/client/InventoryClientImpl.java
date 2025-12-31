package com.festora.orderservice.client;

import com.festora.orderservice.dto.InventoryReserveRequest;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClientImpl implements InventoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${inventory.service.base-url}")
    private String inventoryBaseUrl;

    // POSTPAID dine-in safe default
    private static final int RESERVE_TTL_SECONDS = 2 * 60 * 60; // 2 hours

    /* ===============================
       1️⃣ INITIAL ORDER RESERVE
       =============================== */
    @Override
    public void tempReserve(Order order) {

        InventoryReserveRequest request =
                InventoryReserveRequest.from(order, RESERVE_TTL_SECONDS);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(inventoryBaseUrl + "/inventory/temp-reserve")
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Inventory TEMP reserved for order {}", order.getOrderId());

        } catch (Exception e) {
            log.error("Inventory TEMP reserve failed for order {}", order.getOrderId(), e);
            throw new IllegalStateException("INVENTORY_OUT_OF_STOCK");
        }
    }

    /* ===============================
       2️⃣ ADD MORE ITEMS (INCREMENTAL)
       =============================== */
    @Override
    public void tempReserve(Order order, List<OrderItem> newItems) {

        InventoryReserveRequest request =
                InventoryReserveRequest.from(order, RESERVE_TTL_SECONDS);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(inventoryBaseUrl + "/inventory/temp-reserve")
                    .bodyValue(request)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Inventory TEMP reserve extended for order {}", order.getOrderId());

        } catch (Exception e) {
            log.error("Inventory TEMP reserve failed for add-items {}", order.getOrderId(), e);
            throw new IllegalStateException("ITEM_OUT_OF_STOCK");
        }
    }

    /* ===============================
       3️⃣ CONFIRM AFTER PAYMENT
       =============================== */
    @Override
    public void confirm(String orderId) {

        try {
            webClientBuilder.build()
                    .post()
                    .uri(inventoryBaseUrl + "/inventory/confirm/{orderId}", orderId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Inventory CONFIRMED for order {}", orderId);

        } catch (Exception e) {
            // Retryable error — payment already done
            log.error("Inventory CONFIRM failed for order {}", orderId, e);
            throw new IllegalStateException("INVENTORY_CONFIRM_FAILED");
        }
    }
}