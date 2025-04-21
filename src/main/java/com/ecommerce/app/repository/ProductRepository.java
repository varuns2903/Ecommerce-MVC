package com.ecommerce.app.repository;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> findByCategoryIdAndIdNot(String categoryId, String id);
    List<Product> findByCategoryId(String categoryId);
    List<Product> findByNameContainingIgnoreCase(String name);
}
