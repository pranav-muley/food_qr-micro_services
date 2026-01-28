package com.festora.authservice.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@Entity
@Table(
        name = "qr_table_mapping",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"restaurant_id", "table_number"}
        )
)
public class QrTableMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String qrId;

    @Column(nullable = false)
    private Long restaurantId;

    @Column(nullable = false)
    private Integer tableNumber;

    private Boolean active = true;
}