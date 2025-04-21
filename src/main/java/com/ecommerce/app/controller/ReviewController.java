package com.ecommerce.app.controller;

import com.ecommerce.app.model.Review;
import com.ecommerce.app.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/reviews")
    public String submitReview(@ModelAttribute Review review, @RequestParam String orderId, RedirectAttributes redirectAttributes, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String userEmail = authentication.getName();
            System.out.println("Submitting review for user: " + userEmail + ", product: " + review.getProductId());
            if (reviewService.hasUserReviewedProduct(userEmail, review.getProductId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "You have already reviewed this product.");
                System.out.println("Error: User already reviewed product");
                return "redirect:/orders";
            }
            review.setUserEmail(userEmail);
            reviewService.saveReview(review);
            redirectAttributes.addFlashAttribute("successMessage", "Your review has been submitted successfully!");
            System.out.println("Success: Review submitted for order: " + orderId);
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to submit a review.");
            System.out.println("Error: User not authenticated");
        }
        return "redirect:/orders";
    }
}