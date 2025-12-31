package com.festora.orderservice.service;

import com.festora.orderservice.client.InventoryClient;
import com.festora.orderservice.dto.CreateOrderRequest;
import com.festora.orderservice.dto.GstResult;
import com.festora.orderservice.dto.InventoryReserveRequest;
import com.festora.orderservice.enums.OrderStatus;
import com.festora.orderservice.gst.GstCalculator;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.model.OrderItem;
import com.festora.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final GstCalculator gstCalculator;

    public Order createOrder(CreateOrderRequest req) throws Exception {
        if (ObjectUtils.isEmpty(req)) {
            throw new Exception("Order request cant be empty");
        }

        Order order = buildOrder(req);
        orderRepository.save(order);

        // 🔒 Initial inventory failure CAN cancel order
        try {
            inventoryClient.tempReserve(order);
            order.setStatus(OrderStatus.PENDING);
        } catch (Exception e) {
            order.setStatus(OrderStatus.REJECTED);
            order.setUpdatedAt(now());
            orderRepository.save(order);
            throw new IllegalStateException("OUT_OF_STOCK");
        }
        order.setUpdatedAt(now());
        return orderRepository.save(order);
    }

    /* ===============================
       2️⃣ ADD MORE ITEMS (SAFE)
       =============================== */
    public Order addItems(String orderId, List<OrderItem> newItems) {

        Order order = get(orderId);

        if (!canAddItems(order)) {
            throw new IllegalStateException("Cannot add items now");
        }

        // 🔒 Inventory failure here must NOT cancel order
        try {
            inventoryClient.tempReserve(order, newItems);
        } catch (Exception e) {
            throw new IllegalStateException("ITEM_OUT_OF_STOCK");
        }

        order.getItems().addAll(newItems);
        recalcTotals(order);
        order.setUpdatedAt(now());

        return orderRepository.save(order);
    }

    /* ===============================
       3️⃣ KITCHEN FLOW (NO KITCHEN SERVICE)
       =============================== */
    public void markPreparing(String orderId) {

        Order order = get(orderId);

        // POSTPAID: PAYMENT_PENDING → PREPARING
        // PREPAID : PAID → PREPARING
        if (order.getStatus() == OrderStatus.PAYMENT_PENDING ||
                order.getStatus() == OrderStatus.PAID) {

            order.setStatus(OrderStatus.PREPARING);
            order.setUpdatedAt(now());
            orderRepository.save(order);
            return;
        }

        throw new IllegalStateException("Invalid state for PREPARING");
    }

    public void markServed(String orderId) {
        transition(orderId, OrderStatus.PREPARING, OrderStatus.PAYMENT_PENDING);
    }

    /* ===============================
       4️⃣ BILL REQUEST (POSTPAID)
       =============================== */
    public void requestBill(String orderId) {
        transition(orderId, OrderStatus.PAYMENT_PENDING, OrderStatus.PAYMENT_REQUESTED);
    }

    /* ===============================
       5️⃣ PAYMENT SUCCESS (ONE ENTRY)
       =============================== */
    public void onPaymentSuccess(String orderId) {

        Order order = get(orderId);

        // idempotency
        if (order.getStatus() == OrderStatus.PAID ||
                order.getStatus() == OrderStatus.CLOSED) {
            return;
        }

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING &&
                order.getStatus() != OrderStatus.PAYMENT_REQUESTED) {
            throw new IllegalStateException("Invalid payment state");
        }

        inventoryClient.confirm(orderId);

        order.setStatus(OrderStatus.PAID);
        order.setUpdatedAt(now());
        orderRepository.save(order);
    }

    /* ===============================
       6️⃣ CLOSE ORDER
       =============================== */
    public void closeOrder(String orderId) {
        transition(orderId, OrderStatus.PAID, OrderStatus.CLOSED);
    }

    /* ===============================
       7️⃣ CANCEL ORDER (EXPIRY / ADMIN)
       =============================== */
    public void cancelOrder(String orderId, String reason) {

        Order order = get(orderId);

        // Never cancel paid orders
        if (order.getStatus() == OrderStatus.PAID ||
                order.getStatus() == OrderStatus.CLOSED) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setReason(reason);
        order.setUpdatedAt(now());
        orderRepository.save(order);
    }

    /* ===============================
       STATE TRANSITION GUARD
       =============================== */
    public void transition(String orderId,
                           OrderStatus from,
                           OrderStatus to) {

        Order order = get(orderId);

        if (order.getStatus() != from) {
            throw new IllegalStateException(
                    "Invalid transition: " + from + " → " + order.getStatus()
            );
        }

        order.setStatus(to);
        order.setUpdatedAt(now());
        orderRepository.save(order);
    }

    /* ===============================
       HELPERS
       =============================== */

    private boolean canAddItems(Order order) {
        return order.getStatus() == OrderStatus.PAYMENT_PENDING ||
                order.getStatus() == OrderStatus.PREPARING;
    }

    private Order get(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    private long now() {
        return System.currentTimeMillis();
    }

    private Order buildOrder(CreateOrderRequest req) {

        double base = req.getItems().stream()
                .peek(i -> i.setLineTotal(i.getUnitPrice() * i.getQuantity()))
                .mapToDouble(OrderItem::getLineTotal)
                .sum();

        GstResult gst = gstCalculator.calculate(req.getRestaurantId(), base);

        return Order.builder()
                .orderId(UUID.randomUUID().toString())
                .restaurantId(req.getRestaurantId())
                .sessionId(req.getSessionId())
                .tableNumber(req.getTableNumber())
                .items(req.getItems())
                .baseAmount(base)
                .cgstAmount(gst.getCgst())
                .sgstAmount(gst.getSgst())
                .gstAmount(gst.getTotalTax())
                .totalAmount(base + gst.getTotalTax())
                .status(OrderStatus.CREATED)
                .createdAt(now())
                .updatedAt(now())
                .build();
    }

    private void recalcTotals(Order order) {

        double base = order.getItems().stream()
                .peek(i -> i.setLineTotal(i.getUnitPrice() * i.getQuantity()))
                .mapToDouble(OrderItem::getLineTotal)
                .sum();

        GstResult gst = gstCalculator.calculate(
                order.getRestaurantId(), base
        );

        order.setBaseAmount(base);
        order.setCgstAmount(gst.getCgst());
        order.setSgstAmount(gst.getSgst());
        order.setGstAmount(gst.getTotalTax());
        order.setTotalAmount(base + gst.getTotalTax());
    }

    public Order getOrder(String orderId) {
        if (orderId == null) {
            return null;
        }
        return orderRepository.findById(orderId).orElse(null);
    }

    public void markInventoryReserved(InventoryReserveRequest request) {
        try {
            // add item found in order
            String orderId = request.getOrderId();
            Order order = getOrder(orderId);
//           List<Items> items=  order.getItems().addAll(request.getItems());
            order.setItems(order.getItems());
        } catch (Exception e) {
            log.error("Inventory TEMP reserve failed for add-items {}", request, e);
            throw new IllegalStateException("ITEM_OUT_OF_STOCK");
        }

    }

    public List<Order> getAllPendingOrders() {
        return orderRepository.findOrdersByStatus(OrderStatus.PENDING);
    }

    public Order markOrderConfirm(String orderId) throws Exception {
        if (StringUtils.isEmpty(orderId)) {
            throw new Exception("Invalid order id");
        }

        Order order = orderRepository.findById(orderId).orElse(null);

        if (order == null) {
            throw new Exception("Order not found");
        }

        // confirm inventory ->
        inventoryClient.confirm(orderId);

        // update status
        order.setStatus(OrderStatus.PREPARING);
        order.setUpdatedAt(now());
        return orderRepository.save(order);
    }
}