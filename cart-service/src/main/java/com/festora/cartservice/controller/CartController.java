package com.festora.cartservice.controller;

import com.festora.cartservice.dto.AddToCartRequest;
import com.festora.cartservice.dto.CheckoutRequest;
import com.festora.cartservice.dto.UpdateCartItemRequest;
import com.festora.cartservice.model.Cart;
import com.festora.cartservice.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/items")
    public Cart addItem(@RequestBody AddToCartRequest req) {
        return cartService.addItem(req);
    }

    @GetMapping
    public Cart viewCart(
            @RequestParam Long restaurantId,
            @RequestParam String sessionId
    ) {
        return cartService.getCart(restaurantId, sessionId);
    }

    @DeleteMapping
    public void clearCart(
            @RequestParam Long restaurantId,
            @RequestParam String sessionId
    ) {
        cartService.clearCart(restaurantId, sessionId);
    }


    @PutMapping("/items/{cartItemId}")
    public Cart updateItemQuantity(
            @PathVariable String cartItemId,
            @RequestBody UpdateCartItemRequest req
    ) {
        return cartService.updateItemQuantity(cartItemId, req);
    }

    @DeleteMapping("/items/{cartItemId}")
    public Cart removeItem(
            @PathVariable String cartItemId,
            @RequestParam Long restaurantId,
            @RequestParam String sessionId
    ) {
        return cartService.removeItem(restaurantId, sessionId, cartItemId);
    }

    @PostMapping("/checkout")
    public Object checkout(@RequestBody CheckoutRequest request) {
        return cartService.checkout(request);
    }

}

