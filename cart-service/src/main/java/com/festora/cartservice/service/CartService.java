package com.festora.cartservice.service;

import com.festora.cartservice.model.Cart;
import com.festora.cartservice.model.CartItem;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final long CART_TTL_MINUTES = 60;

    public CartService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Cart getCart(String sessionId) {
        return (Cart) redisTemplate.opsForValue().get(key(sessionId));
    }

    public Cart addItem(String sessionId, CartItem item) {
        Cart cart = getOrCreateCart(sessionId);
        if (cart.getItems().isEmpty()) {
            return null;
        }
        cart.getItems().removeIf(i -> i.getItemId().equals(item.getItemId()));
        cart.getItems().add(item);
        cart.setUpdatedAt(System.currentTimeMillis());
        save(cart);
        return cart;
    }

    public void removeItem(String sessionId, String itemId) {
        Cart cart = getCart(sessionId);
        if (cart == null) return;
        cart.getItems().removeIf(i -> i.getItemId().equals(itemId));
        save(cart);
    }

    public void clear(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }

    private Cart getOrCreateCart(String sessionId) {
        Cart cart = getCart(sessionId);
        return cart != null ? cart : new Cart();
    }

    private void save(Cart cart) {
        redisTemplate.opsForValue().set(
                key(cart.getSessionId()),
                cart,
                CART_TTL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    private String key(String sessionId) {
        return "cart:" + sessionId;
    }
}
