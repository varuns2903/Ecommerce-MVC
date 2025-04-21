package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "carts")
public class Cart {
    @Id
    private String id;

    private String userId;
    private List<ProductItem> items;

    public double getTotalPrice() {
        return items.stream().mapToDouble(ProductItem::getTotalPrice).sum();
    }
}
