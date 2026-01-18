package com.festora.orderservice.controller;

import com.festora.orderservice.dto.CreateOrderRequest;
import com.festora.orderservice.dto.OrderCreateResponse;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.model.OrderItem;
import com.festora.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderCreateResponse> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(OrderCreateResponse.builder()
                            .orderId(order.getOrderId())
                            .status(order.getStatus())
                            .totalAmount(order.getTotalAmount())
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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

//    @PostMapping("/{orderId}/ready")
//    public ResponseEntity<Void> markReady(@PathVariable String orderId) {
//        orderService.markReady(orderId);
//        return ResponseEntity.ok().build();
//    }

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

    @PostMapping("/{orderId}/items")
    public ResponseEntity<Order> addItems(
            @PathVariable String orderId,
            @RequestBody List<OrderItem> items
    ) {
        return ResponseEntity.ok(
                orderService.addItems(orderId, items)
        );
    }

    @PostMapping("/{orderId}/request-bill")
    public ResponseEntity<Void> requestBill(@PathVariable String orderId) {
        orderService.requestBill(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable String orderId,
            @RequestParam(required = false) String reason
    ) {
        orderService.cancelOrder(orderId,
                reason == null ? "MANUAL_CANCEL" : reason);
        return ResponseEntity.ok().build();
    }

}