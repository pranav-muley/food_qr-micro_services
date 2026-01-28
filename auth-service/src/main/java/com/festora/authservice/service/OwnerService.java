package com.festora.authservice.service;

import com.festora.authservice.model.QrTableMapping;
import com.festora.authservice.repository.QrTableMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OwnerService {
    private final QrTableMappingRepository qrTableMappingRepository;

    public String getMappingRestaurantAndTable(Long restaurantId, Integer tableNumber) {
        if (tableNumber == null || tableNumber == 0) {
            throw new IllegalArgumentException("tableNumber must not be null or zero");
        }
        try {
            QrTableMapping tableMapping = null;
            tableMapping = qrTableMappingRepository.findByRestaurantIdAndTableNumber(restaurantId, tableNumber);
            if (org.apache.commons.lang3.ObjectUtils.isNotEmpty(tableMapping)) {
                throw new FileAlreadyExistsException("Table mapping already exists");
            }
            tableMapping = new QrTableMapping();
            tableMapping.setQrId(UUID.randomUUID().toString());
            tableMapping.setRestaurantId(restaurantId);
            tableMapping.setTableNumber(tableNumber);
            tableMapping.setActive(true);

            QrTableMapping saved = qrTableMappingRepository.save(tableMapping);

            return generatingUrlForQR(saved.getQrId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generatingUrlForQR(String qrId) {
        if (qrId == null || qrId.isEmpty()) {
            throw new IllegalArgumentException("qrId must not be null or empty");
        }

        return "http://frontendUrl?qrId=" + URLEncoder.encode(qrId, StandardCharsets.UTF_8);
    }
}
