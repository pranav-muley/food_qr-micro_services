package com.festora.orderservice.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItem {

    private String menuItemId;
    private String variantId;
    private List<String> addonIds;

    private double unitPrice;
    private int quantity;
    private double lineTotal;
}


