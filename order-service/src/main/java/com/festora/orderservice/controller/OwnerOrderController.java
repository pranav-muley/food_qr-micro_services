package com.festora.orderservice.controller;

import com.festora.orderservice.model.Order;
import com.festora.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owner/order")
@RequiredArgsConstructor
public class OwnerOrderController {

    private final OrderService orderService;

    @GetMapping("/pending")
    public ResponseEntity<List<Order>> pendingOrders() {
        try {
           return ResponseEntity.ok(orderService.getAllPendingOrders());
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("confirm/{orderId}")
    public ResponseEntity<Order> confirmOrder(@PathVariable String orderId) {
        try {
            return ResponseEntity.ok(orderService.markOrderConfirm(orderId));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
