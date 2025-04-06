package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId;
    private List<ProductItem> items;
    private String address;
    private OrderStatus status = OrderStatus.NOT_PROCESS;

    public enum OrderStatus {
        NOT_PROCESS, PROCESSING, SHIPPED, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }
    private LocalDateTime createdAt = LocalDateTime.now();

    public double getTotalAmount() {
        return items.stream().mapToDouble(ProductItem::getTotalPrice).sum();
    }
}
