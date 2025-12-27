package com.festora.menuservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("menu_items")
public class MenuItem {

    @Id
    private String id;

    @Indexed
    private Long restaurantId;

    @Indexed
    private String categoryId;

    private String name;
    private String description;

    private Double basePrice;        // renamed from price

    private Boolean veg;
    private Boolean enabled;          // renamed from isAvailable

    private List<Variant> variants;
    private List<Addon> addons;
}