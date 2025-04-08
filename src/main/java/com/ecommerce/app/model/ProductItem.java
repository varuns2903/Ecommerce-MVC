package com.ecommerce.app.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public class ProductItem {
    @DBRef
    private Product product;

    private int quantity;
    private double price;
    private double totalPrice;


    public ProductItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
        this.totalPrice = this.price * this.quantity;
    }

}
