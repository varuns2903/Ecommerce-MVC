package com.ecommerce.app.controller;

import com.ecommerce.app.service.CustomUserDetail;
import com.ecommerce.app.dto.UserDTO;
import com.ecommerce.app.model.Order;
import com.ecommerce.app.model.Product;
import com.ecommerce.app.model.Review;
import com.ecommerce.app.model.User;
import com.ecommerce.app.service.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/product/{id}")
    public String products(@PathVariable String id, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());

        int cartItemCount = 0;
        String currentUserEmail = null;

        if (isAuthenticated) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetail userDetails) {
                Optional<User> userOptional = userService.getUserByEmail(userDetails.getUsername());
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    UserDTO userDTO = new UserDTO(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getAddress());
                    model.addAttribute("loggedInUser", userDTO);
                    cartItemCount = cartService.getCartItemCount(user.getId());
                    currentUserEmail = user.getEmail();
                }
            }
        }

        Product product = productService.getProductById(id);
        List<Product> similarProducts = productService.getSimilarProducts(product.getCategoryId(), id);
        List<Review> reviews = reviewService.getReviewsByProductId(id);
        double averageRating = reviewService.getAverageRating(id);
        long reviewCount = reviewService.getReviewCount(id);

        boolean isDelivered = false;
        if (currentUserEmail != null) {
            List<Order> userOrders = orderService.getOrdersByEmailAndProductId(currentUserEmail, id);
            isDelivered = userOrders.stream().anyMatch(order -> order.getStatus() == Order.OrderStatus.DELIVERED);
        }

        model.addAttribute("cartItemCount", cartItemCount);
        model.addAttribute("product", product);
        model.addAttribute("similarProduct", similarProducts);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", product.getAverageRating());
        model.addAttribute("reviewCount", product.getReviewCount());
        model.addAttribute("orderStatus", isDelivered ? "DELIVERED" : "OTHER");

        return "product-details";
    }

}
