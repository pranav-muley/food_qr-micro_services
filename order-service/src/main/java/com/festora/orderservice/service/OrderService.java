package com.festora.orderservice.service;

import com.festora.orderservice.client.InventoryClient;
import com.festora.orderservice.dto.CreateOrderRequest;
import com.festora.orderservice.dto.GstResult;
import com.festora.orderservice.enums.OrderStatus;
import com.festora.orderservice.gst.GstCalculator;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.model.OrderItem;
import com.festora.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final GstCalculator gstCalculator;

    // ==========================
    // CREATE ORDER
    // ==========================
    public Order createOrder(CreateOrderRequest req) {

        Objects.requireNonNull(req, "Request cannot be null");

        double baseAmount = req.getItems().stream()
                .peek(i -> i.setLineTotal(i.getUnitPrice() * i.getQuantity()))
                .mapToDouble(OrderItem::getLineTotal)
                .sum();

        GstResult gst = gstCalculator.calculate(req.getRestaurantId(), baseAmount);

        double totalAmount = baseAmount + gst.getTotalTax();

        Order order = Order.builder()
                .orderId(UUID.randomUUID().toString())
                .restaurantId(req.getRestaurantId())
                .sessionId(req.getSessionId())
                .tableNumber(req.getTableNumber())
                .items(req.getItems())
                .baseAmount(baseAmount)

                // GST breakup stored
                .cgstAmount(gst.getCgst())
                .sgstAmount(gst.getSgst())
                .gstAmount(gst.getTotalTax())

                .discountAmount(0)
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .createdAt(System.currentTimeMillis())
                .updatedAt(System.currentTimeMillis())
                .build();

        orderRepository.save(order);

        // TEMP inventory reservation
        inventoryClient.tempReserve(order);

        return order;
    }

    // ==================================================
    // GET ORDER
    // ==================================================
    public Order getOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    // ==================================================
    // PAYMENT CALLBACK
    // ==================================================
    public void onPaymentSuccess(String orderId) {

        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            return; // idempotent
        }

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(System.currentTimeMillis());
        orderRepository.save(order);

        // confirm inventory after payment
        inventoryClient.confirm(orderId);

        // move to kitchen
        order.setStatus(OrderStatus.PREPARING);
        order.setUpdatedAt(System.currentTimeMillis());
        orderRepository.save(order);
    }

    // ==================================================
    // KITCHEN WORKFLOW
    // ==================================================
    public void markPreparing(String orderId) {
        transition(orderId, OrderStatus.PAID, OrderStatus.PREPARING);
    }

    public void markReady(String orderId) {
        transition(orderId, OrderStatus.PREPARING, OrderStatus.READY);
    }

    public void markServed(String orderId) {
        transition(orderId, OrderStatus.READY, OrderStatus.SERVED);
    }

    public void closeOrder(String orderId) {
        transition(orderId, OrderStatus.SERVED, OrderStatus.CLOSED);
    }

    // ==================================================
    // INVENTORY EVENTS (Kafka)
    // ==================================================
    public void markInventoryReserved(String orderId) {

        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            return;
        }

        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setUpdatedAt(System.currentTimeMillis());
        orderRepository.save(order);
    }

    public void markInventoryFailed(String orderId) {

        Order order = getOrder(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(System.currentTimeMillis());
        orderRepository.save(order);
    }

    // ==================================================
    // STATE TRANSITION GUARD (IMPORTANT)
    // ==================================================
    private void transition(String orderId, OrderStatus from, OrderStatus to) {

        Order order = getOrder(orderId);

        if (order.getStatus() != from) {
            throw new IllegalStateException(
                    "Invalid transition: " + order.getStatus() + " → " + to
            );
        }

        order.setStatus(to);
        order.setUpdatedAt(System.currentTimeMillis());
        orderRepository.save(order);
    }
}
