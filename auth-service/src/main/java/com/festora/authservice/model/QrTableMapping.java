package com.festora.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "qr_table_mapping")
@Data
public class QrTableMapping {

    @Id
    @Column(length = 40)
    private String qrId;            // e.g. Qr_9xA72

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private String tableNumber;

    @Column(nullable = false)
    private boolean active = true;
}

