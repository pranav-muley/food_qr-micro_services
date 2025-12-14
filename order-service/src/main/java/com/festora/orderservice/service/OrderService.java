package com.festora.orderservice.service;

import com.festora.orderservice.enums.OrderStatus;
import com.festora.orderservice.model.Order;
import com.festora.orderservice.model.OrderDto;
import com.festora.orderservice.repository.OrderRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;

@Service
public class OrderService {
    private static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new Random();

    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;

    public OrderService(OrderRepository orderRepository, MongoTemplate mongoTemplate) {
        this.orderRepository = orderRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Order getOrderByOrderIdUserId(String orderId, String userId) throws Exception {
        if (StringUtils.isAnyBlank(orderId, userId)) {
            throw new IllegalArgumentException("orderId Or userId is Blank");
        }

        Order order = orderRepository.findByOrderIdAndUserId(orderId, userId);
        if (ObjectUtils.isEmpty(order)) {
            throw new Exception("Order Not found");
        }
        return order;
    }

    public String createOrder(OrderDto orderDto) {
        if (ObjectUtils.isEmpty(orderDto)) return "ORDER NOT CREATED";
        Order order = buildOrder(orderDto);
        if( ObjectUtils.isEmpty(order) ){
            return "ORDER_FAILED_TO_CREATE";
        }
        orderRepository.save(order);
        return  "ORDER CREATED";
    }

    public OrderDto updateOrder (OrderDto orderDto) {
        if (ObjectUtils.isEmpty(orderDto)) return null;
        Order order = orderRepository.findByOrderIdAndUserId(orderDto.getOrderId(), orderDto.getUserId());
        if (ObjectUtils.isEmpty(order)) {
            return null;
        }
        order.setStatus(orderDto.getStatus());
        order.setOrderProducts(orderDto.getOrderProducts());
        order.setDateModified(System.currentTimeMillis());

        Query query = Query.query(Criteria.where("orderId").is(order.getOrderId()));
        Update update = new Update()
                .set("orderProducts", orderDto.getOrderProducts())
                .set("dateModified", System.currentTimeMillis())
                .set("status", orderDto.getStatus());
        mongoTemplate.updateFirst(query, update, Order.class);
        return buildOrderDtoFromOrder(order);
    }

    public static String generateOrderId() {
        StringBuilder letters = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            letters.append(LETTERS.charAt(RANDOM.nextInt(LETTERS.length())));
        }

        int numbers = 1000 + RANDOM.nextInt(9000);

        return "ORDID-" + letters + "-" + numbers;
    }

    private Order buildOrder(OrderDto orderDto) {
        if (ObjectUtils.isEmpty(orderDto)) return null;

        Order order = new Order();
        order.setOrderId(generateOrderId());
        order.setUserId(orderDto.getUserId());
        order.setOrderProducts(orderDto.getOrderProducts());
        order.setStatus(orderDto.getStatus());
        order.setDateModified(System.currentTimeMillis());
        order.setDateCreated(System.currentTimeMillis());
        return order;
    }

    private OrderDto buildOrderDtoFromOrder(Order order) {
        if (ObjectUtils.isEmpty(order)) return null;

        return OrderDto.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .status(order.getStatus())
                .orderProducts(order.getOrderProducts())
                .build();
    }

    public void markInventoryReserved(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // 🔒 Idempotency
        if (order.getStatus() != OrderStatus.CREATED.name()) {
            return;
        }

        order.setStatus(OrderStatus.INVENTORY_RESERVED.name());
        orderRepository.save(order);
    }

    public void markInventoryFailed(String orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED.name() ||
                order.getStatus() == OrderStatus.INVENTORY_FAILED.name()) {
            return;
        }

        order.setStatus(OrderStatus.INVENTORY_FAILED.name());
        orderRepository.save(order);
    }
}
