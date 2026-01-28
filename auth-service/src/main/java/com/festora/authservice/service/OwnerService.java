package com.festora.authservice.service;

import com.festora.authservice.model.QrTableMapping;
import com.festora.authservice.repository.QrTableMappingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.nio.file.FileAlreadyExistsException;

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
            tableMapping.setRestaurantId(restaurantId);
            tableMapping.setTableNumber(String.valueOf(tableNumber));
            QrTableMapping savedModel = qrTableMappingRepository.save(tableMapping);
            if (ObjectUtils.isEmpty(savedModel)) {
                throw new IllegalArgumentException("Something went wrong, while saving table mapping");
            }
            return generatingUrlForQR(savedModel.getQrId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String generatingUrlForQR(String qrId) {
        if (qrId == null || qrId.isEmpty()) {
            throw new IllegalArgumentException("qrId must not be null or empty");
        }

        return "http://frontendUrl?qrId=" + qrId;
    }
}
