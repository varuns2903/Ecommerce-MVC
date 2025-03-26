package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "categories")
public class Category {
    @Id
    private String id;

    private String name;
    private String slug;

    public Category() {
    }

    public Category(String name, String slug) {
        this.name = name;
        this.slug = slug.toLowerCase();
    }
}
