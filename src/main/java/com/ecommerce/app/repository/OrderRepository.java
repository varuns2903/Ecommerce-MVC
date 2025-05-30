package com.ecommerce.app.repository;

import com.ecommerce.app.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    Optional<Order> findById(String orderId);
    List<Order> findByUserEmailAndItemsProductId(String email, String productId);

}
