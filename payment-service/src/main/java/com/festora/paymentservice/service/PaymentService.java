package com.festora.paymentservice.service;

import com.festora.paymentservice.enums.PaymentMethod;
import com.festora.paymentservice.enums.PaymentMode;
import com.festora.paymentservice.enums.PaymentStatus;
import com.festora.paymentservice.model.PaymentLedger;
import com.festora.paymentservice.model.PaymentOutbox;
import com.festora.paymentservice.repository.PaymentLedgerRepository;
import com.festora.paymentservice.repository.PaymentOutboxRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentLedgerRepository ledgerRepo;
    private final PaymentOutboxRepository outboxRepo;

    public String createPayment(
            String orderId,
            double amount,
            String currency,
            PaymentMode mode,
            PaymentMethod method
    ) {

        String paymentId = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();

        // MOCK success for now
        PaymentStatus status = PaymentStatus.SUCCESS;

        PaymentLedger ledger = PaymentLedger.builder()
                .paymentId(paymentId)
                .orderId(orderId)
                .amount(amount)
                .currency(currency)
                .paymentMode(mode)
                .paymentMethod(method)
                .status(status)
                .createdAt(now)
                .build();

        ledgerRepo.save(ledger);

        String eventType = status == PaymentStatus.SUCCESS
                ? "payment.success"
                : "payment.failed";

        PaymentOutbox outbox = PaymentOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(eventType)
                .aggregateId(orderId)
                .payload(buildPayload(ledger))
                .published(false)
                .createdAt(now)
                .build();

        outboxRepo.save(outbox);

        return paymentId;
    }

    private String buildPayload(PaymentLedger ledger) {
        return String.format("""
            {
              "orderId": "%s",
              "paymentId": "%s",
              "amount": %.2f,
              "currency": "%s",
              "paymentMode": "%s",
              "method": "%s",
              "timestamp": %d
            }
            """,
                ledger.getOrderId(),
                ledger.getPaymentId(),
                ledger.getAmount(),
                ledger.getCurrency(),
                ledger.getPaymentMode(),
                ledger.getPaymentMethod(),
                ledger.getCreatedAt()
        );
    }
}
