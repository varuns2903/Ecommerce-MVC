package com.ecommerce.app.repository;

import com.ecommerce.app.model.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByProductId(String productId);
    boolean existsByUserEmailAndProductId(String userEmail, String productId);
}