package com.festora.cartservice.controller;

import com.festora.cartservice.dto.AddItemRequest;
import com.festora.cartservice.model.Cart;
import com.festora.cartservice.model.CartItem;
import com.festora.cartservice.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/items")
    public Cart addItem(
            @RequestHeader("X-Session-Id") String sessionId,
            @RequestBody AddItemRequest req
    ) {
        CartItem item = new CartItem(
                req.getItemId(),
                req.getName(),
                req.getPrice(),
                req.getQty()
        );
        return cartService.addItem(sessionId, item);
    }

    @GetMapping
    public Cart getCart(@RequestHeader("X-Session-Id") String sessionId) {
        return cartService.getCart(sessionId);
    }

    @DeleteMapping
    public void clear(@RequestHeader("X-Session-Id") String sessionId) {
        cartService.clear(sessionId);
    }
}

