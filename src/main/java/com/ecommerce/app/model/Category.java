package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String id;

    private String name;
    private String slug;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Category() {
    }

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug.toLowerCase();
    }
}
