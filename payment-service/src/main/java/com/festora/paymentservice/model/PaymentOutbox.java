package com.festora.paymentservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_outbox")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOutbox {

    @Id
    private String eventId;

    private String eventType; // payment.success | payment.failed
    private String aggregateId; // orderId

    @Lob
    private String payload;

    private boolean published;
    private long createdAt;
}

