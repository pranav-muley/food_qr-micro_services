package com.festora.cartservice.client;

import com.festora.cartservice.dto.MenuValidationRequest;
import com.festora.cartservice.dto.MenuValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class MenuClient {

    private final WebClient webClient;

    @Value("${services.menu.base-url}")
    private String menuBaseUrl;

    public MenuClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public MenuValidationResult validateAndFetch(
            Long restaurantId,
            String menuItemId,
            String variantId,
            java.util.List<String> addonIds
    ) {
        MenuValidationRequest request = MenuValidationRequest.builder()
                .restaurantId(restaurantId)
                .menuItemId(menuItemId)
                .variantId(variantId)
                .addonIds(addonIds)
                .build();

        return webClient.post()
                .uri(menuBaseUrl + "/menu/validate")
                .header("X-API-KEY", "SECRET123")   // ✅ REQUIRED
                .header("Content-Type", "application/json")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MenuValidationResult.class)
                .block();
    }
}