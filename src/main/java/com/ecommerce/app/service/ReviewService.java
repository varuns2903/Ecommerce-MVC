package com.ecommerce.app.service;

import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.repository.ProductRepository;
import com.ecommerce.app.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Review> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId);
    }

    public Review saveReview(Review review) {
        review.setTimestamp(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        updateProductRating(review.getProductId());
        return savedReview;
    }

    public boolean hasUserReviewedProduct(String userEmail, String productId) {
        System.out.println("Checking review for user: " + userEmail + ", product: " + productId);
        boolean exists = reviewRepository.existsByUserEmailAndProductId(userEmail, productId);
        System.out.println("Review exists: " + exists);
        return exists;
    }

    public double getAverageRating(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getAverageRating() : 0.0;
    }

    public long getReviewCount(String productId) {
        Product product = productRepository.findById(productId).orElse(null);
        return product != null ? product.getReviewCount() : 0;
    }

    private void updateProductRating(String productId) {
        List<Review> reviews = reviewRepository.findByProductId(productId);
        Product product = productRepository.findById(productId).orElse(null);
        if (product != null) {
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
    }
}