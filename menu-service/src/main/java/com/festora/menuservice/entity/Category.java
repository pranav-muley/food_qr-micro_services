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
@Document("categories")
public class Category {

    @Id
    private String id;

    @Indexed
    private Long restaurantId;

    private String name;
    private String description;

    private List<MenuItem> items;
}
