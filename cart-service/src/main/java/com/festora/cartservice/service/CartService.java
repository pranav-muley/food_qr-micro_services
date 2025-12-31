package com.festora.cartservice.service;

import com.festora.cartservice.client.MenuClient;
import com.festora.cartservice.dto.AddToCartRequest;
import com.festora.cartservice.dto.CheckoutRequest;
import com.festora.cartservice.dto.MenuValidationResult;
import com.festora.cartservice.dto.UpdateCartItemRequest;
import com.festora.cartservice.dto.client.OrderCreateRequest;
import com.festora.cartservice.dto.client.OrderItem;
import com.festora.cartservice.model.AddonSnapshot;
import com.festora.cartservice.model.Cart;
import com.festora.cartservice.model.CartItem;
import com.festora.cartservice.repository.CartRedisRepository;
import com.festora.cartservice.client.OrderClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRedisRepository cartRepo;
    private final MenuClient menuClient;
    private final OrderClient orderClient;

    private String buildKey(Long restaurantId, String sessionId) {
        return "cart:" + restaurantId + ":" + sessionId;
    }

    public Cart addItem(AddToCartRequest req) {

        if (ObjectUtils.isEmpty(req) || req.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be >= 1");
        }

        String key = buildKey(req.getRestaurantId(), req.getSessionId());
        Cart cart;
        try {
            cart = cartRepo.get(key);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }

        if (cart == null) {
            long now = System.currentTimeMillis();

            cart = Cart.builder()
                    .cartId(UUID.randomUUID().toString())
                    .restaurantId(req.getRestaurantId())
                    .sessionId(req.getSessionId())
                    .createdAt(now)
                    .updatedAt(now)
                    .items(new ArrayList<>())
                    .subtotal(0)
                    .build();
        }

        MenuValidationResult menuResult = menuClient.validateAndFetch(req.getRestaurantId(), req.getMenuItemId(),
                req.getVariantId(), req.getAddonIds());

        double unitPrice =
                menuResult.getVariantPrice()
                        + menuResult.getAddons()
                        .stream()
                        .mapToDouble(AddonSnapshot::getPrice)
                        .sum();

        String identityKey = buildIdentity(req.getMenuItemId(), req.getVariantId(),
                req.getAddonIds());

        Optional<CartItem> existing =
                cart.getItems().stream()
                        .filter(i -> i.getIdentityKey().equals(identityKey))
                        .findFirst();

        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + req.getQuantity());
            item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
        } else {
            CartItem item = CartItem.builder()
                    .cartItemId(UUID.randomUUID().toString())
                    .identityKey(identityKey)
                    .menuItemId(req.getMenuItemId())
                    .name(menuResult.getItemName())
                    .variant(menuResult.getVariant())
                    .addons(menuResult.getAddons())
                    .unitPrice(unitPrice)
                    .quantity(req.getQuantity())
                    .totalPrice(unitPrice * req.getQuantity())
                    .build();

            cart.getItems().add(item);
        }

        recalcSubtotal(cart);
        cart.setUpdatedAt(System.currentTimeMillis());

        cartRepo.save(key, cart);
        return cart;
    }

    public Cart getCart(Long restaurantId, String sessionId) {
        Cart cart = cartRepo.get(buildKey(restaurantId, sessionId));
        return cart == null ? emptyCart(restaurantId, sessionId) : cart;
    }

    public void clearCart(Long restaurantId, String sessionId) {
        cartRepo.delete(buildKey(restaurantId, sessionId));
    }

    private void recalcSubtotal(Cart cart) {
        cart.setSubtotal(
                cart.getItems().stream()
                        .mapToDouble(CartItem::getTotalPrice)
                        .sum()
        );
    }

    private String buildIdentity(
            String menuItemId,
            String variantId,
            List<String> addonIds
    ) {
        List<String> sorted =
                addonIds == null ? new ArrayList<>() : new ArrayList<>(addonIds);

        Collections.sort(sorted);

        return menuItemId
                + "|" + (variantId == null ? "NA" : variantId)
                + "|" + String.join(",", sorted);
    }

    private Cart emptyCart(Long restaurantId, String sessionId) {
        long now = System.currentTimeMillis();

        return Cart.builder()
                .cartId(UUID.randomUUID().toString())
                .restaurantId(restaurantId)
                .sessionId(sessionId)
                .createdAt(now)
                .updatedAt(now)
                .items(new ArrayList<>())
                .subtotal(0)
                .build();
    }

    public Cart updateItemQuantity(
            String cartItemId,
            UpdateCartItemRequest req
    ) {
        if (req == null || req.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be >= 1");
        }

        String key = buildKey(req.getRestaurantId(), req.getSessionId());
        Cart cart = cartRepo.get(key);

        if (cart == null) {
            throw new NoSuchElementException("Cart not found");
        }

        CartItem item =
                cart.getItems().stream()
                        .filter(i -> i.getCartItemId().equals(cartItemId))
                        .findFirst()
                        .orElseThrow(() ->
                                new NoSuchElementException("Cart item not found")
                        );

        item.setQuantity(req.getQuantity());
        item.setTotalPrice(item.getUnitPrice() * req.getQuantity());

        recalcSubtotal(cart);
        cart.setUpdatedAt(System.currentTimeMillis());

        cartRepo.save(key, cart);
        return cart;
    }

    public Cart removeItem(
            Long restaurantId,
            String sessionId,
            String cartItemId
    ) {
        String key = buildKey(restaurantId, sessionId);
        Cart cart = cartRepo.get(key);

        if (cart == null) {
            throw new NoSuchElementException("Cart not found");
        }

        boolean removed =
                cart.getItems().removeIf(
                        item -> item.getCartItemId().equals(cartItemId)
                );

        if (!removed) {
            throw new NoSuchElementException("Cart item not found");
        }

        recalcSubtotal(cart);
        cart.setUpdatedAt(System.currentTimeMillis());

        // Optional: if cart empty, you may delete key
        if (cart.getItems().isEmpty()) {
            cartRepo.delete(key);
            return emptyCart(restaurantId, sessionId);
        }

        cartRepo.save(key, cart);
        return cart;
    }

    public Object checkout(CheckoutRequest req) {

        String key = buildKey(req.getRestaurantId(), req.getSessionId());
        Cart cart;
        try {
            cart = cartRepo.get(key);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new NoSuchElementException("Cart not found");
        }

        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cart expired or empty");
        }

        // 🔁 Final menu re-validation (NO price recalculation)
        cart.getItems().forEach(item ->
                menuClient.validateAndFetch(
                        req.getRestaurantId(),
                        item.getMenuItemId(),
                        item.getVariant() == null ? null : item.getVariant().getVariantId(),
                        item.getAddons()
                                .stream()
                                .map(a -> a.getAddonId())
                                .toList()
                )
        );

        // 📦 Build Order request snapshot
        OrderCreateRequest orderRequest =
                OrderCreateRequest.builder()
                        .restaurantId(cart.getRestaurantId())
                        .sessionId(cart.getSessionId())
                        .subtotal(cart.getSubtotal())
                        .items(
                                cart.getItems().stream()
                                        .map(item -> OrderItem.builder()
                                                .menuItemId(item.getMenuItemId())
                                                .variantId(
                                                        item.getVariant() == null
                                                                ? null
                                                                : item.getVariant().getVariantId()
                                                )
                                                .addonIds(
                                                        item.getAddons().stream()
                                                                .map(AddonSnapshot::getAddonId)
                                                                .toList()
                                                )
                                                .unitPrice(item.getUnitPrice())
                                                .quantity(item.getQuantity())
                                                .totalPrice(item.getTotalPrice())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build();

        // 🚀 Call Order Service
        Object orderResponse = orderClient.createOrder(orderRequest);

        // 🧹 Clear cart only on success
        cartRepo.delete(key);

        return orderRequest;
    }
}