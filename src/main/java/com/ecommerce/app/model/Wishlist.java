package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Data
@Document(collection = "wishlist")
public class Wishlist {

    @Id
    private String id;

    private String userId;

    @DBRef
    private List<Product> products = new ArrayList<>();
}
