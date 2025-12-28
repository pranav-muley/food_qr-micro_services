package com.festora.orderservice.controller;

import com.festora.orderservice.dto.CreateOrderRequest;
import com.festora.orderservice.dto.OrderCreateResponse;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderCreateResponse> createOrder(
            @RequestBody CreateOrderRequest request
    ) {
        Order order = orderService.createOrder(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrderCreateResponse.builder()
                        .orderId(order.getOrderId())
                        .status(order.getStatus())
                        .totalAmount(order.getTotalAmount())
                        .build());
    }

    // ==================================================
    // 2) Get Order (UI / Admin / Kitchen)
    // ==================================================
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    // ==================================================
    // 3) Payment Callback (Success)
    // ==================================================
    @PostMapping("/{orderId}/payment/success")
    public ResponseEntity<Void> paymentSuccess(@PathVariable String orderId) {
        orderService.onPaymentSuccess(orderId);
        return ResponseEntity.ok().build();
    }

    // ==================================================
    // 4) Kitchen Workflow
    // ==================================================
    @PostMapping("/{orderId}/prepare")
    public ResponseEntity<Void> markPreparing(@PathVariable String orderId) {
        orderService.markPreparing(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/ready")
    public ResponseEntity<Void> markReady(@PathVariable String orderId) {
        orderService.markReady(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/served")
    public ResponseEntity<Void> markServed(@PathVariable String orderId) {
        orderService.markServed(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/close")
    public ResponseEntity<Void> closeOrder(@PathVariable String orderId) {
        orderService.closeOrder(orderId);
        return ResponseEntity.ok().build();
    }
}