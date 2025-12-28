package com.festora.orderservice.client;

import com.festora.orderservice.dto.InventoryReserveRequest;
import com.festora.orderservice.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryClientImpl implements InventoryClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${inventory.service.base-url}")
    private String inventoryBaseUrl;

    private static final int RESERVE_TTL_SECONDS = 300; // 5 min

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

            log.info("TEMP reserve request sent for order {}", order.getOrderId());

        } catch (Exception e) {
            log.error("Failed to TEMP reserve inventory for order {}", order.getOrderId(), e);
            // ❗ DO NOT update order here
            // Inventory will emit inventory.failed if needed
        }
    }


    @Override
    public void confirm(String orderId) {

        try {
            webClientBuilder.build()
                    .post()
                    .uri(inventoryBaseUrl + "/inventory/confirm/{orderId}", orderId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

//            log.info("Inventory confirmed for order {}", orderId);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}