package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "products")
public class Product {
    @Id
    private String id;

    private String name;
    private String slug;
    private String description;
    private double price;

    @DBRef
    private Category category;  // Reference to Category

    private int quantity;
    private byte[] photoData;   // Store photo as byte array
    private String photoContentType;
    private boolean shipping;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
