package com.ecommerce.app.config;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ProductMigration implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public void run(String... args) throws Exception {
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            List<Review> reviews = reviewRepository.findByProductId(product.getId());
            if (!reviews.isEmpty()) {
                double sum = reviews.stream().mapToInt(Review::getRating).sum();
                double averageRating = sum / reviews.size();
                product.setAverageRating(averageRating);
                product.setReviewCount(reviews.size());
            } else {
                product.setAverageRating(0.0);
                product.setReviewCount(0);
            }
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
        }
        System.out.println("Product migration completed: Updated averageRating and reviewCount for all products.");
    }
}