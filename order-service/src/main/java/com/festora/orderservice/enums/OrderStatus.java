package com.festora.orderservice.enums;

public enum OrderStatus {
    CREATED,
    INVENTORY_RESERVED,
    PAYMENT_PENDING,
    PAID,
    PREPARING,
    READY,
    SERVED,
    CLOSED,
    INVENTORY_FAILED,
    PAYMENT_REQUESTED,  // Bill frozen, waiting for payment
    CANCELLED
}

