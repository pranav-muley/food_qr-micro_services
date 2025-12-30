package com.festora.paymentservice.repository;

import com.festora.paymentservice.model.PaymentOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentOutboxRepository
        extends JpaRepository<PaymentOutbox, String> {

    List<PaymentOutbox> findByPublishedFalse();
}
