package com.festora.paymentservice.repository;

import com.festora.paymentservice.model.PaymentLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentLedgerRepository
        extends JpaRepository<PaymentLedger, String> {
}
